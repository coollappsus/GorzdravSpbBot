package com.example.gorzdrav_spb_bot.config;

import com.example.gorzdrav_spb_bot.TelegramBot;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.TimeZone;

/**
 * Конфигурация приложения
 *
 */
@Configuration
public class AppConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .timeZone(TimeZone.getTimeZone("Europe/Moscow"))
                .featuresToDisable(
                        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE
                );
    }

    @Bean
    public TelegramBotsApi getTelegramBotsApi(TelegramBot telegramBot) throws TelegramApiException {
        var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(telegramBot);
        return telegramBotsApi;
    }
}
