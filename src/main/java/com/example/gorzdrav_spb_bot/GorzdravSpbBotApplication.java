package com.example.gorzdrav_spb_bot;

import api.longpoll.bots.exceptions.VkApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class GorzdravSpbBotApplication {

    public static void main(String[] args) throws VkApiException {
        ConfigurableApplicationContext context = SpringApplication.run(GorzdravSpbBotApplication.class, args);
        VkBot app = context.getBean(VkBot.class);
//        app.initialize();

        // Запускаем бесконечный цикл "живучести"
        while (true) {
            long countDaysSleep = 0;
            try {
                log.info("Запуск LongPoll сессии...");
                app.startPolling();
            } catch (VkApiException e) {
                // Тот самый таймаут или любая другая сетевая ошибка упадет сюда
                try {
                    if (e.getMessage().contains("Rate limit reached")) {
                        countDaysSleep++;
                        log.error("Поймали лимит запросов к серверу. Придется ждать долго. Спим столько суток - {}", countDaysSleep);
                        TimeUnit.DAYS.sleep(countDaysSleep);
                    } else {
                        log.error("Сетевая ошибка VK (возможно таймаут). Пробую переподключиться через 3 минуты...", e);
                        // Обязательно делаем паузу, чтобы не долбить сервер в случае жесткого сбоя
                        TimeUnit.MINUTES.sleep(3);
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break; // Если поток прерван, выходим
                }
            } catch (Exception e) {
                // Если случилось что-то совсем страшное
                log.error("Критическая ошибка в логике! Проверь код.", e);
                break;
            }
        }
    }

}
