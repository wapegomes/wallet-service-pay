package com.walletservice.service;

import com.walletservice.domain.Wallet;
import com.walletservice.dto.BalanceResponse;
import com.walletservice.dto.CreateWalletRequest;
import com.walletservice.dto.DepositRequest;
import com.walletservice.dto.WithdrawalRequest;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SimpleWalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private final String userId = "testUser";
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = new Wallet(null, userId, new BigDecimal("100.00"), "BRL");
    }

    @Test
    void getBalance_ReturnsCorrectBalance() {
        // Arrange
        when(walletRepository.findByIdUsuario(userId)).thenReturn(testWallet);

        // Act
        BalanceResponse response = walletService.getBalance(userId);

        // Assert
        assertEquals(userId, response.idUsuario());
        assertEquals(new BigDecimal("100.00"), response.saldo());
        assertEquals("BRL", response.moeda());
        verify(walletRepository).findByIdUsuario(userId);
    }

    @Test
    void getBalance_ThrowsExceptionWhenWalletNotFound() {
        // Arrange
        String nonExistentUserId = "nonExistentUser";
        when(walletRepository.findByIdUsuario(nonExistentUserId)).thenReturn(null);

        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> walletService.getBalance(nonExistentUserId));
        verify(walletRepository).findByIdUsuario(nonExistentUserId);
    }

    @Test
    void createWallet_CreatesWalletSuccessfully() {
        // Arrange
        String newUserId = "newUser";
        CreateWalletRequest request = new CreateWalletRequest(newUserId);
        Wallet newWallet = new Wallet(null, newUserId, BigDecimal.ZERO, "BRL");

        when(walletRepository.findByIdUsuario(newUserId)).thenReturn(null);
        when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);

        // Act
        Wallet result = walletService.createWallet(request);

        // Assert
        assertNotNull(result);
        assertEquals(newUserId, result.getIdUsuario());
        assertEquals(BigDecimal.ZERO, result.getSaldo());
        assertEquals("BRL", result.getMoeda());

        verify(walletRepository).findByIdUsuario(newUserId);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void deposit_IncreasesBalanceCorrectly() {
        // Arrange
        BigDecimal depositAmount = new BigDecimal("50.00");
        BigDecimal expectedBalance = new BigDecimal("150.00");
        DepositRequest request = new DepositRequest(userId, depositAmount);

        when(walletRepository.findByIdUsuario(userId)).thenReturn(testWallet);

        Wallet updatedWallet = new Wallet(null, userId, expectedBalance, "BRL");
        when(walletRepository.save(any(Wallet.class))).thenReturn(updatedWallet);

        // Act
        Wallet result = walletService.deposit(request);

        // Assert
        assertEquals(expectedBalance, result.getSaldo());
        verify(walletRepository).findByIdUsuario(userId);
        verify(walletRepository).save(any(Wallet.class));
        verify(transactionRepository).save(any());
    }

    @Test
    void withdraw_DecreasesBalanceCorrectly() {
        // Arrange
        BigDecimal withdrawAmount = new BigDecimal("50.00");
        BigDecimal expectedBalance = new BigDecimal("50.00");
        WithdrawalRequest request = new WithdrawalRequest(userId, withdrawAmount);

        when(walletRepository.findByIdUsuario(userId)).thenReturn(testWallet);

        Wallet updatedWallet = new Wallet(null, userId, expectedBalance, "BRL");
        when(walletRepository.save(any(Wallet.class))).thenReturn(updatedWallet);

        // Act
        Wallet result = walletService.withdraw(request);

        // Assert
        assertEquals(expectedBalance, result.getSaldo());
        verify(walletRepository).findByIdUsuario(userId);
        verify(walletRepository).save(any(Wallet.class));
        verify(transactionRepository).save(any());
    }
}
