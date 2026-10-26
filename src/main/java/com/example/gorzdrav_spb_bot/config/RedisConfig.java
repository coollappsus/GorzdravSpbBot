package com.example.gorzdrav_spb_bot.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientCustomizer() {
        return clientConfigurationBuilder -> {
            // Настройки на уровне сетевых сокетов ОС в контейнере
            SocketOptions socketOptions = SocketOptions.builder()
                    .keepAlive(true) // ОС будет слать keep-alive пакеты, защищая от Netfilter Docker
                    .connectTimeout(Duration.ofSeconds(2)) // Синхронизируем таймаут сокета со Spring
                    .build();

            // Опции клиента Lettuce
            ClientOptions clientOptions = ClientOptions.builder()
                    .socketOptions(socketOptions)
                    .pingBeforeActivateConnection(false) 
                    .timeoutOptions(TimeoutOptions.enabled())
                    .build();

            clientConfigurationBuilder.clientOptions(clientOptions);
        };
    }
}
