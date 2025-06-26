package com.walletservice.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configuração padrão para todos os caches
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL de 10 minutos
                .disableCachingNullValues() // Não armazena valores nulos
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Criando o cache manager com a configuração
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                // Configurações específicas para diferentes caches
                .withCacheConfiguration("walletBalances",
                        cacheConfig.entryTtl(Duration.ofMinutes(5))) // Cache de saldos expira em 5 minutos
                .withCacheConfiguration("userWallets",
                        cacheConfig.entryTtl(Duration.ofHours(1))) // Cache de carteiras expira em 1 hora
                .build();
    }
}
