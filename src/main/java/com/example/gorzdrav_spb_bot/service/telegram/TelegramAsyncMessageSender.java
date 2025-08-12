package com.example.gorzdrav_spb_bot.service.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class TelegramAsyncMessageSender {

    private final DefaultAbsSender defaultAbsSender;

    public TelegramAsyncMessageSender(@Lazy DefaultAbsSender defaultAbsSender) {
        this.defaultAbsSender = defaultAbsSender;
    }

    /**
     * Удобный метод для отправки сообщений пользователю асинхронно
     */
    @Async
    public void sendMessageToUser(Long chatId, String message) {
        SendMessage sm = SendMessage.builder()
                .chatId(chatId.toString())
                .text(message)
                .build();
        try {
            defaultAbsSender.execute(sm);
        } catch (TelegramApiException e) {
            log.error(String.format("Ошибка отправки ответа клиенту с chatId=%s, message=%s", chatId, message), e);
            throw new RuntimeException(e);
        }
    }
}
