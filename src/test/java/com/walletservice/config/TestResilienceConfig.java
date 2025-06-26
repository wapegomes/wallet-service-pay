package com.walletservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@TestConfiguration
public class TestResilienceConfig {

    @Bean
    @Primary
    public CircuitBreakerRegistry testCircuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(2)
                .minimumNumberOfCalls(1)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    @Primary
    public RetryRegistry testRetryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(1)
                .waitDuration(Duration.ofMillis(100))
                .build();

        return RetryRegistry.of(config);
    }
}
