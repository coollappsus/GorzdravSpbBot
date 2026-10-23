package com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.add;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class AddMedCardLastNameHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_MIDDLE_NAME = "Введите имя пациента";

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        userState.getMedicalCardContext().setLastName(message.getText());
        userState.setHandlerName("AddMedCardFirstNameHandler");
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(RESPONSE_TEXT_MIDDLE_NAME)
                .build();
    }
}
