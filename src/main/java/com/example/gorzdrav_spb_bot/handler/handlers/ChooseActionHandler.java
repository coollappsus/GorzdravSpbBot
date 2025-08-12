package com.example.gorzdrav_spb_bot.handler.handlers;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.cancel.CancelAppointmentDistrictHandler;
import com.example.gorzdrav_spb_bot.handler.handlers.create.CreateAppointmentDistrictHandler;
import com.example.gorzdrav_spb_bot.handler.handlers.find.FindAppointmentDistrictHandler;
import com.example.gorzdrav_spb_bot.handler.handlers.tracking.TrackingDistrictHandler;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.*;

@Component
public class ChooseActionHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_DISCRICT = "Выберите район";
    private static final String RESPONSE_TEXT_REMOVE = "Мед.карта удалена";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final MedicalCardRepository medicalCardRepository;
    private final CreateAppointmentDistrictHandler createAppointmentDistrictHandler;
    private final FindAppointmentDistrictHandler findAppointmentDistrictHandler;
    private final CancelAppointmentDistrictHandler cancelAppointmentDistrictHandler;
    private final StartHandler startHandler;
    private final TrackingDistrictHandler trackingDistrictHandler;

    public ChooseActionHandler(GorzdravService gorzdravService, KeyboardFactory keyboardFactory,
                               MedicalCardRepository medicalCardRepository,
                               CreateAppointmentDistrictHandler createAppointmentDistrictHandler,
                               FindAppointmentDistrictHandler findAppointmentDistrictHandler,
                               CancelAppointmentDistrictHandler cancelAppointmentDistrictHandler,
                               @Lazy StartHandler startHandler, TrackingDistrictHandler trackingDistrictHandler) {
        this.gorzdravService = gorzdravService;
        this.keyboardFactory = keyboardFactory;
        this.medicalCardRepository = medicalCardRepository;
        this.createAppointmentDistrictHandler = createAppointmentDistrictHandler;
        this.findAppointmentDistrictHandler = findAppointmentDistrictHandler;
        this.cancelAppointmentDistrictHandler = cancelAppointmentDistrictHandler;
        this.startHandler = startHandler;
        this.trackingDistrictHandler = trackingDistrictHandler;
    }

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        var districtsName = gorzdravService.getDistricts().stream()
                .map(District::name)
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(districtsName);

        if (message.getText().equals(CREATE_APPOINTMENT.getText())) {
            userState.setHandler(createAppointmentDistrictHandler);
        } else if (message.getText().equals(CANCEL_APPOINTMENT.getText())) {
            userState.setHandler(cancelAppointmentDistrictHandler);
        } else if (message.getText().equals(FIND_APPOINTMENT.getText())) {
            userState.setHandler(findAppointmentDistrictHandler);
        } else if (message.getText().equals(TRACKING_APPOINTMENT.getText())) {
            userState.setHandler(trackingDistrictHandler);
        } else if (message.getText().equals(REMOVE.getText())) {
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

        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_DISCRICT)
                .build();
    }
}
