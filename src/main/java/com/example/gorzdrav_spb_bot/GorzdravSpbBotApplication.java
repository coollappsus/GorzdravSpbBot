package com.example.gorzdrav_spb_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class GorzdravSpbBotApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        SpringApplication.run(GorzdravSpbBotApplication.class, args);
    }

}
