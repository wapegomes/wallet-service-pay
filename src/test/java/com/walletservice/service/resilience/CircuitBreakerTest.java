package com.walletservice.service.resilience;

import com.walletservice.domain.Wallet;
import com.walletservice.dto.BalanceResponse;
import com.walletservice.repository.TransactionRepository;
import com.walletservice.repository.WalletRepository;
import com.walletservice.service.WalletService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.walletservice.config.TestSecurityConfig;
import com.walletservice.config.TestResilienceConfig;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import({TestSecurityConfig.class, TestResilienceConfig.class})
@ActiveProfiles("test")
public class CircuitBreakerTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    private Wallet testWallet;
    private final String userId = "testUser";

    @BeforeEach
    void setUp() {
        testWallet = new Wallet(UUID.randomUUID(), userId, new BigDecimal("100.00"), "BRL");
    }

    @Test
    void testCircuitBreakerState() {
        // Get the circuit breaker instance
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("walletService");

        // Initially the circuit breaker should be closed
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    @Test
    void testGetBalanceFallback() {
        // Configure the mock to throw an exception
        when(walletRepository.findByIdUsuario(anyString())).thenThrow(new RuntimeException("Database connection error"));

        // Call the service method that should trigger the fallback
        BalanceResponse response = walletService.getBalance(userId);

        // Verify the fallback response
        assertNotNull(response);
        assertEquals(userId, response.idUsuario());
        assertEquals(BigDecimal.ZERO, response.saldo());
        assertEquals("BRL", response.moeda());
    }

    @Test
    void testCircuitBreakerMetrics() {
        // Get the circuit breaker instance
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("walletService");

        // Configure the mock to throw an exception
        when(walletRepository.findByIdUsuario(anyString())).thenThrow(new RuntimeException("Database connection error"));

        // Call the service method multiple times to trigger the circuit breaker
        for (int i = 0; i < 5; i++) {
            walletService.getBalance(userId);
        }

        // Get metrics
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        // Verify that failed calls were recorded
        assertTrue(metrics.getNumberOfFailedCalls() > 0);
    }
}
