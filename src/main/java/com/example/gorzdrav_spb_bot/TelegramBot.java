package com.example.gorzdrav_spb_bot;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageDispatcher;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final String BOT_USER_NAME = "Coollappsus gorzdrav spb bot";
    private static Set<UserState> states = new HashSet<>();

    private final TelegramUpdateMessageDispatcher telegramUpdateMessageHandler;
    private final StartHandler startHandler;

    public TelegramBot(
            @Value("${token.bot}") String botToken,
            TelegramUpdateMessageDispatcher telegramUpdateMessageHandler,
            StartHandler startHandler
    ) {
        super(new DefaultBotOptions(), botToken);
        this.telegramUpdateMessageHandler = telegramUpdateMessageHandler;
        this.startHandler = startHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            var method = processUpdate(update);
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

    private BotApiMethod<?> processUpdate(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        UserState state = states.stream().filter(s -> s.getUserId().equals(userId)).findFirst()
                .orElse(null);
        if (state == null) {
            state = UserState.builder()
                    .handler(startHandler)
                    .context(new HashSet<>())
                    .userId(userId)
                    .build();
            states.add(state);
        }

        return update.hasMessage()
                ? telegramUpdateMessageHandler.processMessage(update.getMessage(), state)
                : null;
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