package com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.add;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class AddMedCardLastNameHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_MIDDLE_NAME = "Введите отчество пациента";

    private final AddMedCardMiddleNameHandler addMedCardMiddleNameHandler;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        userState.getContext().stream()
                .filter(mc -> mc instanceof MedicalCard)
                .map(mc -> (MedicalCard) mc)
                .peek(mc -> mc.setLastName(message.getText()))
                .findFirst()
                .orElseThrow();
        userState.setHandler(addMedCardMiddleNameHandler);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(RESPONSE_TEXT_MIDDLE_NAME)
                .build();
    }
}
