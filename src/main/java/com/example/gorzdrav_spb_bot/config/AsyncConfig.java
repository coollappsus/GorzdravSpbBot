package com.example.gorzdrav_spb_bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync // Включает поддержку @Async во всем приложении
public class AsyncConfig {

    @Bean(name = "botExecutor")
    public Executor botExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);        // 5 потоков всегда готовы к работе
        executor.setMaxPoolSize(15);       // Если очередь забьется, расширимся до 15
        executor.setQueueCapacity(50);     // Очередь на 50 входящих сообщений
        executor.setThreadNamePrefix("VK-Worker-");
        // Если всё забито, обработает в основном потоке (не даст потерять сообщение)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
