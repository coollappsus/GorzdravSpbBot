package com.example.gorzdrav_spb_bot;

import api.longpoll.bots.exceptions.VkApiException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GorzdravSpbBotApplication {

    public static void main(String[] args) throws VkApiException {
        ConfigurableApplicationContext context = SpringApplication.run(GorzdravSpbBotApplication.class, args);
        VkBot app = context.getBean(VkBot.class);
        app.initialize();
        app.startPolling();
    }

}
