package com.walletservice.service;

import com.walletservice.domain.Transaction;
import com.walletservice.domain.Wallet;
import com.walletservice.dto.BalanceResponse;
import com.walletservice.dto.CreateWalletRequest;
import com.walletservice.dto.DepositRequest;
import com.walletservice.dto.TransferRequest;
import com.walletservice.dto.WithdrawalRequest;
import com.walletservice.exception.InsufficientFundsException;
import com.walletservice.exception.WalletAlreadyExistsException;
import com.walletservice.exception.WalletNotFoundException;
import com.walletservice.repository.TransactionRepository;
import com.walletservice.repository.WalletRepository;
import com.walletservice.utils.Messages;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    // TODO: Adicionar suporte para múltiplas moedas no futuro
    private static final String DEFAULT_CURRENCY = "BRL";

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    @CachePut(value = "userWallets", key = "#request.idUsuario()")
    public Wallet createWallet(CreateWalletRequest request) {
        // Verifica se já existe uma carteira para este usuário
        if (walletRepository.findByIdUsuario(request.idUsuario()) != null) {
            throw new WalletAlreadyExistsException(String.format(Messages.WALLET_ALREADY_EXISTS, request.idUsuario()));
        }

        // Cria uma nova carteira com saldo zero
        Wallet newWallet = new Wallet(null, request.idUsuario(), BigDecimal.ZERO, DEFAULT_CURRENCY);
        return walletRepository.save(newWallet);
    }

    @Transactional
    @CacheEvict(value = {"walletBalances", "userWallets"}, key = "#request.idUsuario()")
    @CircuitBreaker(name = "walletService", fallbackMethod = "depositFallback")
    @Retry(name = "walletService")
    public Wallet deposit(DepositRequest request) {
        // Busca a carteira ou lança exceção se não existir
        Wallet wallet = findWalletByIdUsuario(request.idUsuario());

        // Valida o valor do depósito
        validateAmount(request.valor());

        // Atualiza o saldo
        wallet.setSaldo(wallet.getSaldo().add(request.valor()));
        walletRepository.save(wallet);

        // Registra a transação
        createTransaction(request.idUsuario(), null, Messages.TRANSACTION_TYPE_DEPOSIT,
                          request.valor(), Messages.DEPOSIT_SUCCESS);

        return wallet;
    }

    @Transactional
    @CacheEvict(value = {"walletBalances", "userWallets"}, key = "#request.idUsuario()")
    @CircuitBreaker(name = "walletService", fallbackMethod = "withdrawFallback")
    @Retry(name = "walletService")
    public Wallet withdraw(WithdrawalRequest request) {
        Wallet wallet = findWalletByIdUsuario(request.idUsuario());
        validateAmount(request.valor());

        // Verifica se há saldo suficiente
        if (wallet.getSaldo().compareTo(request.valor()) < 0) {
            throw new InsufficientFundsException(Messages.INSUFFICIENT_BALANCE);
        }

        // Atualiza o saldo - poderia usar um método helper aqui, mas deixei direto por simplicidade
        wallet.setSaldo(wallet.getSaldo().subtract(request.valor()));
        walletRepository.save(wallet);

        // Registra a transação
        createTransaction(request.idUsuario(), null, Messages.TRANSACTION_TYPE_WITHDRAW,
                          request.valor(), Messages.WITHDRAW_SUCCESS);

        return wallet;
    }

    @Transactional
    @CacheEvict(value = {"walletBalances", "userWallets"}, allEntries = true)
    @CircuitBreaker(name = "walletService", fallbackMethod = "transferFallback")
    @Retry(name = "walletService")
    public void transfer(TransferRequest request) {
        // Não permite transferir para si mesmo
        if (Objects.equals(request.idUsuarioOrigem(), request.idUsuarioDestino())) {
            throw new IllegalArgumentException("Não é possível transferir para si mesmo");
        }

        // Busca as carteiras
        Wallet sourceWallet = findWalletByIdUsuario(request.idUsuarioOrigem());
        Wallet destinationWallet = findWalletByIdUsuario(request.idUsuarioDestino());

        validateAmount(request.valor());

        // Verifica saldo
        if (sourceWallet.getSaldo().compareTo(request.valor()) < 0) {
            throw new InsufficientFundsException(Messages.INSUFFICIENT_BALANCE);
        }

        // Realiza a transferência
        sourceWallet.setSaldo(sourceWallet.getSaldo().subtract(request.valor()));
        destinationWallet.setSaldo(destinationWallet.getSaldo().add(request.valor()));

        // Salva as alterações
        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);

        // Registra a transação
        createTransaction(request.idUsuarioOrigem(), request.idUsuarioDestino(),
                          Messages.TRANSACTION_TYPE_TRANSFER, request.valor(),
                          Messages.TRANSFER_SUCCESS);
    }

    @Cacheable(value = "walletBalances", key = "#idUsuario", unless = "#result == null")
    @CircuitBreaker(name = "walletService", fallbackMethod = "getBalanceFallback")
    // Removendo a anotação @TimeLimiter que estava causando problemas
    @Retry(name = "walletService")
    public BalanceResponse getBalance(String idUsuario) {
        logger.info("Fetching balance for user: {}", idUsuario);
        // Esta operação agora será cacheada pelo Redis
        Wallet wallet = findWalletByIdUsuario(idUsuario);
        return new BalanceResponse(wallet.getIdUsuario(), wallet.getSaldo(), wallet.getMoeda());
    }

    // Fallback method for getBalance
    public BalanceResponse getBalanceFallback(String idUsuario, Exception ex) {
        logger.error("Circuit breaker fallback: getBalance failed for user {}", idUsuario, ex);
        // Return a default response or cached data
        return new BalanceResponse(idUsuario, BigDecimal.ZERO, "BRL");
    }

    // Método para calcular saldo histórico
    // Poderia ser otimizado no futuro usando uma abordagem de snapshot
    @CircuitBreaker(name = "walletService", fallbackMethod = "getHistoricalBalanceFallback")
    @Retry(name = "walletService")
    public BalanceResponse getHistoricalBalance(String idUsuario, LocalDateTime dateTime) {
        Wallet wallet = findWalletByIdUsuario(idUsuario);
        List<Transaction> transactions = transactionRepository.findByIdUsuarioOrigemAndDataHoraLessThanEqual(idUsuario, dateTime);

        BigDecimal historicalBalance = BigDecimal.ZERO;

        // Calcula o saldo histórico com base nas transações
        for (Transaction t : transactions) {
            if (t.getTipoTransacao().equals(Messages.TRANSACTION_TYPE_DEPOSIT)) {
                historicalBalance = historicalBalance.add(t.getValor());
            } else if (t.getTipoTransacao().equals(Messages.TRANSACTION_TYPE_WITHDRAW)) {
                historicalBalance = historicalBalance.subtract(t.getValor());
            } else if (t.getTipoTransacao().equals(Messages.TRANSACTION_TYPE_TRANSFER)) {
                if (t.getIdUsuarioOrigem().equals(idUsuario)) {
                    historicalBalance = historicalBalance.subtract(t.getValor());
                } else if (t.getIdUsuarioDestino() != null && t.getIdUsuarioDestino().equals(idUsuario)) {
                    historicalBalance = historicalBalance.add(t.getValor());
                }
            }
        }

        return new BalanceResponse(wallet.getIdUsuario(), historicalBalance, wallet.getMoeda());
    }

    // Helper para buscar carteira - agora com cache
    @Cacheable(value = "userWallets", key = "#idUsuario", unless = "#result == null")
    public Wallet findWalletByIdUsuario(String idUsuario) {
        Wallet wallet = walletRepository.findByIdUsuario(idUsuario);
        if (wallet == null) {
            throw new WalletNotFoundException(String.format(Messages.WALLET_NOT_FOUND, idUsuario));
        }
        return wallet;
    }

    // Validação básica de valor
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(Messages.INVALID_AMOUNT);
        }
    }

    private void createTransaction(String sourceUserId, String destinationUserId,
                                  String type, BigDecimal amount, String description) {
        Transaction transaction = new Transaction(
            null, type, amount, DEFAULT_CURRENCY,
            LocalDateTime.now(), sourceUserId, destinationUserId,
            "COMPLETED", description
        );
        transactionRepository.save(transaction);
    }

    // Fallback methods for circuit breaker

    public Wallet depositFallback(DepositRequest request, Exception ex) {
        logger.error("Circuit breaker fallback: deposit failed for user {}", request.idUsuario(), ex);
        throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    }

    public Wallet withdrawFallback(WithdrawalRequest request, Exception ex) {
        logger.error("Circuit breaker fallback: withdraw failed for user {}", request.idUsuario(), ex);
        throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    }

    public void transferFallback(TransferRequest request, Exception ex) {
        logger.error("Circuit breaker fallback: transfer failed from user {} to {}",
                  request.idUsuarioOrigem(), request.idUsuarioDestino(), ex);
        throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    }

    public BalanceResponse getHistoricalBalanceFallback(String idUsuario, LocalDateTime dateTime, Exception ex) {
        logger.error("Circuit breaker fallback: getHistoricalBalance failed for user {}", idUsuario, ex);
        // Return current balance as fallback
        try {
            return getBalance(idUsuario);
        } catch (Exception e) {
            return new BalanceResponse(idUsuario, BigDecimal.ZERO, "BRL");
        }
    }
}
