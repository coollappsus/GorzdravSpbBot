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
public class AddMedCardLastNameHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_MIDDLE_NAME = "Введите отчество пациента";

    private final AddMedCardMiddleNameHandler addMedCardMiddleNameHandler;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        userState.getContext().stream()
                .filter(mc -> mc instanceof MedicalCard)
                .map(mc -> (MedicalCard) mc)
                .peek(mc -> mc.setLastName(message.getText()))
                .findFirst()
                .orElseThrow();
        userState.setHandler(addMedCardMiddleNameHandler);
        return VkResponse.builder()
                .message(RESPONSE_TEXT_MIDDLE_NAME)
                .build();
    }
}
