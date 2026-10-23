package com.example.gorzdrav_spb_bot.handler.handlers;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.UserConstResponseText;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.remove.MedicalCardRemoveHandler;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class ChooseActionHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_DISCRICT = "Выберите район";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final MedicalCardRemoveHandler medicalCardRemoveHandler;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        var districtsName = gorzdravService.getDistricts().stream()
                .map(District::name)
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(districtsName);

        String text = message.getText();
        UserConstResponseText command = UserConstResponseText.fromText(text);
        switch (command) {
            case CREATE_APPOINTMENT   -> userState.setHandlerName("CreateAppointmentDistrictHandler");
            case CANCEL_APPOINTMENT   -> userState.setHandlerName("CancelAppointmentDistrictHandler");
            case FIND_APPOINTMENT     -> userState.setHandlerName("FindAppointmentDistrictHandler");
            case TRACKING_APPOINTMENT -> userState.setHandlerName("TrackingDistrictHandler");
            case REMOVE               -> { return medicalCardRemoveHandler.processMessage(message, userState); }
            default                   ->
                    throw new IllegalStateException("Неизвестная команда: " + text + ". Выберите команду из списка");
        }

        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_DISCRICT)
                .build();
    }
}
