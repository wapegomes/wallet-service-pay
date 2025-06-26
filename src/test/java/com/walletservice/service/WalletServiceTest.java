package com.walletservice.service;

import com.walletservice.domain.Wallet;
import com.walletservice.dto.CreateWalletRequest;
import com.walletservice.dto.DepositRequest;
import com.walletservice.dto.TransferRequest;
import com.walletservice.dto.WithdrawalRequest;
import com.walletservice.exception.InsufficientFundsException;
import com.walletservice.exception.WalletAlreadyExistsException;
import com.walletservice.exception.WalletNotFoundException;
import com.walletservice.repository.TransactionRepository;
import com.walletservice.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet wallet;

    // Configuração inicial para cada teste
    @BeforeEach
    void setUp() {
        // Cria uma carteira com saldo inicial de 100
        wallet = new Wallet(UUID.randomUUID(), "user1", new BigDecimal("100.00"), "BRL");
    }

    @Test
    void createWallet_Success() {
        // Configura o mock para simular que não existe carteira para o usuário
        when(walletRepository.findByIdUsuario("user1")).thenReturn(null);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // Executa o método que estamos testando
        CreateWalletRequest request = new CreateWalletRequest("user1");
        Wallet createdWallet = walletService.createWallet(request);

        // Verifica o resultado
        assertNotNull(createdWallet);
        assertEquals("user1", createdWallet.getIdUsuario());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_AlreadyExists() {
        // Configura o mock para simular que já existe uma carteira
        when(walletRepository.findByIdUsuario("user1")).thenReturn(wallet);

        // Cria a requisição
        CreateWalletRequest request = new CreateWalletRequest("user1");

        // Verifica se a exceção é lançada
        assertThrows(WalletAlreadyExistsException.class, () -> walletService.createWallet(request));
    }

    @Test
    void deposit_Success() {
        // Configura os mocks
        when(walletRepository.findByIdUsuario("user1")).thenReturn(wallet);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // Executa o depósito
        DepositRequest request = new DepositRequest("user1", new BigDecimal("50.00"));
        Wallet updatedWallet = walletService.deposit(request);

        // Verifica se o saldo foi atualizado corretamente (100 + 50 = 150)
        assertEquals(new BigDecimal("150.00"), updatedWallet.getSaldo());

        // Verifica se a transação foi registrada
        verify(transactionRepository).save(any());
    }

    @Test
    void withdraw_Success() {
        // Configura os mocks
        when(walletRepository.findByIdUsuario("user1")).thenReturn(wallet);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // Executa o saque
        WithdrawalRequest request = new WithdrawalRequest("user1", new BigDecimal("50.00"));
        Wallet updatedWallet = walletService.withdraw(request);

        // Verifica se o saldo foi atualizado corretamente (100 - 50 = 50)
        assertEquals(new BigDecimal("50.00"), updatedWallet.getSaldo());

        // Verifica se a transação foi registrada
        verify(transactionRepository).save(any());
    }

    @Test
    void withdraw_InsufficientFunds() {
        // Configura o mock
        when(walletRepository.findByIdUsuario("user1")).thenReturn(wallet);

        // Tenta sacar mais do que tem na conta
        WithdrawalRequest request = new WithdrawalRequest("user1", new BigDecimal("200.00"));

        // Verifica se a exceção é lançada
        assertThrows(InsufficientFundsException.class, () -> walletService.withdraw(request));
    }

    @Test
    void transfer_Success() {
        // Cria uma carteira de destino
        Wallet destinationWallet = new Wallet(UUID.randomUUID(), "user2", new BigDecimal("50.00"), "BRL");

        // Configura os mocks
        when(walletRepository.findByIdUsuario("user1")).thenReturn(wallet);
        when(walletRepository.findByIdUsuario("user2")).thenReturn(destinationWallet);

        // Executa a transferência
        TransferRequest request = new TransferRequest("user1", "user2", new BigDecimal("50.00"));
        walletService.transfer(request);

        // Verifica se as carteiras foram salvas e a transação registrada
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository).save(any());
    }

    @Test
    void transfer_InsufficientFunds() {
        // Cria uma carteira de destino
        Wallet destinationWallet = new Wallet(UUID.randomUUID(), "user2", new BigDecimal("50.00"), "BRL");

        // Configura os mocks
        when(walletRepository.findByIdUsuario("user1")).thenReturn(wallet);
        when(walletRepository.findByIdUsuario("user2")).thenReturn(destinationWallet);

        // Tenta transferir mais do que tem na conta
        TransferRequest request = new TransferRequest("user1", "user2", new BigDecimal("200.00"));

        // Verifica se a exceção é lançada
        assertThrows(InsufficientFundsException.class, () -> walletService.transfer(request));
    }

    // Teste para verificar se o saldo é retornado corretamente
    @Test
    void getBalance_Success() {
        when(walletRepository.findByIdUsuario("user1")).thenReturn(wallet);
        var balance = walletService.getBalance("user1");
        assertEquals(new BigDecimal("100.00"), balance.saldo());
    }

    // Teste para verificar se a exceção é lançada quando a carteira não existe
    @Test
    void getBalance_WalletNotFound() {
        when(walletRepository.findByIdUsuario("user1")).thenReturn(null);
        assertThrows(WalletNotFoundException.class, () -> walletService.getBalance("user1"));
    }
}
