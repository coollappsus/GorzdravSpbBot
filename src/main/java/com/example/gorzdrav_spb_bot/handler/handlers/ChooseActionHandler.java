package com.example.gorzdrav_spb_bot.handler.handlers;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.cancel.CancelAppointmentDistrictHandler;
import com.example.gorzdrav_spb_bot.handler.handlers.create.CreateAppointmentDistrictHandler;
import com.example.gorzdrav_spb_bot.handler.handlers.find.FindAppointmentDistrictHandler;
import com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.remove.MedicalCardRemoveHandler;
import com.example.gorzdrav_spb_bot.handler.handlers.tracking.TrackingDistrictHandler;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.*;

@Component
@AllArgsConstructor
public class ChooseActionHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_DISCRICT = "Выберите район";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final CreateAppointmentDistrictHandler createAppointmentDistrictHandler;
    private final FindAppointmentDistrictHandler findAppointmentDistrictHandler;
    private final CancelAppointmentDistrictHandler cancelAppointmentDistrictHandler;
    private final MedicalCardRemoveHandler medicalCardRemoveHandler;
    private final TrackingDistrictHandler trackingDistrictHandler;

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
            medicalCardRemoveHandler.processMessage(message, userState);
        } else {
            throw new RuntimeException("Unknown message type: " + message.getText());
        }

        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_DISCRICT)
                .build();
    }
}
