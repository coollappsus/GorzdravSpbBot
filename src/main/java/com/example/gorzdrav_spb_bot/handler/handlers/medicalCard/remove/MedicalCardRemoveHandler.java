package com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.remove;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import api.longpoll.bots.model.objects.basic.Message;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MedicalCardRemoveHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_REMOVE = "Мед.карта удалена";

    private final MedicalCardRepository medicalCardRepository;
    private final StartHandler startHandler;

    public MedicalCardRemoveHandler(MedicalCardRepository medicalCardRepository, @Lazy StartHandler startHandler) {
        this.medicalCardRepository = medicalCardRepository;
        this.startHandler = startHandler;
    }

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        userState.getContext().stream()
                .filter(mc -> mc instanceof MedicalCard)
                .map(MedicalCard.class::cast)
                .findFirst()
                .ifPresent(medicalCardRepository::delete);
        userState.setHandler(startHandler);
        return VkResponse.builder()
                .message(RESPONSE_TEXT_REMOVE)
                .build();
    }
}
