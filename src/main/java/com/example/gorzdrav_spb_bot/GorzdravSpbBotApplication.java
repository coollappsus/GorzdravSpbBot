package com.example.gorzdrav_spb_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GorzdravSpbBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(GorzdravSpbBotApplication.class, args);
    }

}
