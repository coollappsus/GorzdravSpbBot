package com.example.gorzdrav_spb_bot.service.gorzdrav;

import com.example.gorzdrav_spb_bot.service.gorzdrav.api.GorzdravClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class GorzdravConfiguration {

    @Bean
    public GorzdravClient gorzdravClient(RestTemplateBuilder restTemplateBuilder) {
        return new GorzdravClient(restTemplateBuilder.build());
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(5);
        exec.setMaxPoolSize(10);
        exec.setQueueCapacity(50);
        exec.initialize();
        return exec;
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("sched-");
        return scheduler;
    }
}
