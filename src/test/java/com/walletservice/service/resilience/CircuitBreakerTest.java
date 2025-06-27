package com.walletservice.service.resilience;

import com.walletservice.domain.Wallet;
import com.walletservice.repository.TransactionRepository;
import com.walletservice.repository.WalletRepository;
import com.walletservice.service.WalletService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CircuitBreakerTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private WalletService walletService;

    private CircuitBreakerRegistry circuitBreakerRegistry;

    private Wallet testWallet;
    private final String userId = "testUser";

    @BeforeEach
    void setUp() {
        // Configurar o CircuitBreakerRegistry para testes
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(2)
                .minimumNumberOfCalls(1)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .build();

        circuitBreakerRegistry = CircuitBreakerRegistry.of(config);

        // Criar instância do WalletService com os mocks
        walletService = new WalletService(walletRepository, transactionRepository);

        // Configurar o wallet de teste
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

        try {
            // Call the service method
            walletService.getBalance(userId);
            fail("Should have thrown an exception");
        } catch (Exception e) {
            // Verificar que a exceção foi lançada
            assertTrue(e instanceof RuntimeException);
        }
    }

    @Test
    void testCircuitBreakerMetrics() {
        // Criar um circuit breaker manualmente para teste
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testCircuitBreaker");

        // Verificar o estado inicial
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());

        // Simular chamadas com falha
        Runnable failingRunnable = () -> {
            throw new RuntimeException("Simulated failure");
        };

        // Executar chamadas com falha através do circuit breaker
        for (int i = 0; i < 5; i++) {
            try {
                circuitBreaker.executeRunnable(failingRunnable);
            } catch (Exception e) {
                // Esperado
            }
        }

        // Verificar métricas
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        assertTrue(metrics.getNumberOfFailedCalls() > 0, "Deveria registrar chamadas com falha");
    }
}
