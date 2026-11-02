package com.example.gorzdrav_spb_bot.handler.handlers.tracking.cancel;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.TaskContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
@AllArgsConstructor
public class TrackingCancelChooseHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_CANCEL = "Выберите номер отслеживание для отмены";

    private final KeyboardFactory keyboardFactory;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        List<String> idsListString = userState.getTasks()
                .stream()
                .map(TaskContext::getId)
                .map(String::valueOf)
                .map(s -> "Номер отслеживания - " + s)
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(idsListString);
        userState.setHandlerName("TrackingCancelHandler");
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_CANCEL)
                .build();
    }
}
