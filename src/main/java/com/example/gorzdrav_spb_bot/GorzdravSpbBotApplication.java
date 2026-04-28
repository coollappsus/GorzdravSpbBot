package com.example.gorzdrav_spb_bot;

import api.longpoll.bots.exceptions.VkApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class GorzdravSpbBotApplication {

    public static void main(String[] args) throws VkApiException {
        ConfigurableApplicationContext context = SpringApplication.run(GorzdravSpbBotApplication.class, args);
        VkBot app = context.getBean(VkBot.class);
        app.initialize();

        // Запускаем бесконечный цикл "живучести"
        while (true) {
            try {
                log.info("Запуск LongPoll сессии...");
                app.startPolling();
            } catch (VkApiException e) {
                // Тот самый таймаут или любая другая сетевая ошибка упадет сюда
                log.error("Сетевая ошибка VK (возможно таймаут). Пробую переподключиться через 10 секунд...", e);
                try {
                    // Обязательно делаем паузу, чтобы не долбить сервер в случае жесткого сбоя
                    Thread.sleep(100000);
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
