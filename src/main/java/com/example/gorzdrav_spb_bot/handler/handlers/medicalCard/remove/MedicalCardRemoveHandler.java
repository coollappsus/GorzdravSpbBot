package com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.remove;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MedicalCardRemoveHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_REMOVE = "Мед.карта удалена";

    private final MedicalCardRepository medicalCardRepository;
    private final StartHandler startHandler;

    public MedicalCardRemoveHandler(MedicalCardRepository medicalCardRepository, @Lazy StartHandler startHandler) {
        this.medicalCardRepository = medicalCardRepository;
        this.startHandler = startHandler;
    }

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        userState.getContext().stream()
                .filter(mc -> mc instanceof MedicalCard)
                .map(MedicalCard.class::cast)
                .findFirst()
                .ifPresent(medicalCardRepository::delete);
        userState.setHandler(startHandler);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(RESPONSE_TEXT_REMOVE)
                .build();
    }
}
