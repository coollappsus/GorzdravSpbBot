package com.example.gorzdrav_spb_bot.handler;

import com.example.gorzdrav_spb_bot.handler.cache.UserStateService;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TelegramUpdateMessageDispatcher {

    private static final String CLEAR_CONTEXT_RESPONSE = "Весь контекст очищен, начнем сначала";

    private final UserStateService userStateService;
    private final List<TelegramUpdateMessageHandler> handlers;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    public BotApiMethod<?> processUpdate(Update update) {
        var chatId = update.getMessage().getChatId();
        UserState userState = userStateService.findByUserId(chatId)
                .orElseGet(() -> UserState.builder()
                        .userId(chatId)
                        .handlerName("StartHandler")
                        .build());

        return update.hasMessage()
                ? processMessage(update.getMessage(), userState)
                : null;
    }

    private BotApiMethod<?> processMessage(Message message, UserState userState) {
        log.info("Начало обработки сообщения: message={}", message);

        if (message.isCommand() && message.getText().equals("/clear")) {
            userStateService.deleteByUserId(message.getChatId());
            userState = UserState.builder()
                    .userId(message.getChatId())
                    .handlerName("StartHandler")
                    .build();
            userStateService.save(userState);
            telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), CLEAR_CONTEXT_RESPONSE);
        }

        UserState finalUserState = userState;
        TelegramUpdateMessageHandler currentHandler = handlers.stream()
                .filter(h -> ClassUtils.getUserClass(h).getSimpleName().equals(finalUserState.getHandlerName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Хэндлер не найден: " + finalUserState.getHandlerName()));
        var botApiMethod = currentHandler.processMessage(message, finalUserState);
        userStateService.save(finalUserState);
        return botApiMethod;
    }
}
