package com.example.gorzdrav_spb_bot;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final String BOT_USER_NAME = "Coollappsus gorzdrav spb bot";

    private final TelegramUpdateMessageDispatcher telegramUpdateMessageHandler;

    public TelegramBot(
            @Value("${token.bot}") String botToken,
            TelegramUpdateMessageDispatcher telegramUpdateMessageHandler
    ) {
        super(new DefaultBotOptions(), botToken);
        this.telegramUpdateMessageHandler = telegramUpdateMessageHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            var method = telegramUpdateMessageHandler.processUpdate(update);
            if (method != null ) {
                sendApiMethod(method);
            }
        } catch (Exception e) {
            log.error("Error while processing update", e);
            try {
                sendUserErrorMessage(update.getMessage().getChatId(), e.getMessage());
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void sendUserErrorMessage(Long userId, String errorMessage) throws TelegramApiException {
        sendApiMethod(SendMessage.builder()
                .chatId(userId)
                .text("Произошла ошибка, попробуйте позже или обратитесь к админу. \nОшибка: " + errorMessage)
                .build());
    }

    @Override
    public String getBotUsername() {
        return BOT_USER_NAME;
    }
}