package com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.add;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.vk.api.sdk.objects.messages.Message;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AddMedCardFirstNameHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_LAST_NAME = "Введите фамилию пациента";

    private final AddMedCardLastNameHandler addMedCardLastNameHandler;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        userState.getContext().stream()
                .filter(mc -> mc instanceof MedicalCard)
                .map(mc -> (MedicalCard) mc)
                .peek(mc -> mc.setFirstName(message.getText()))
                .findFirst()
                .orElseThrow();
        userState.setHandler(addMedCardLastNameHandler);
        return VkResponse.builder()
                .message(RESPONSE_TEXT_LAST_NAME)
                .build();
    }
}
