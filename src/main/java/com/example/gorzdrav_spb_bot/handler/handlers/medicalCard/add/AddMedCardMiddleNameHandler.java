package com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.add;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import api.longpoll.bots.model.objects.basic.Message;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AddMedCardMiddleNameHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_BIRTH_DATE = "Введите дату рождения пациента в формате dd.mm.yyyy";

    private final AddMedCardBirthDateHandler addMedCardBirthDateHandler;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        userState.getContext().stream()
                .filter(mc -> mc instanceof MedicalCard)
                .map(mc -> (MedicalCard) mc)
                .peek(mc -> mc.setMiddleName(message.getText()))
                .findFirst()
                .orElseThrow();
        userState.setHandler(addMedCardBirthDateHandler);
        return VkResponse.builder()
                .message(RESPONSE_TEXT_BIRTH_DATE)
                .build();
    }
}
