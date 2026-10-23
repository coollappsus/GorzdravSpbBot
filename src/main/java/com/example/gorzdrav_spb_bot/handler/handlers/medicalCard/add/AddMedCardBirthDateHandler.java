package com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.add;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@AllArgsConstructor
public class AddMedCardBirthDateHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_DISCRICT = "Выберите район";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        Date date = stringToDate(message.getText());
        userState.getMedicalCardContext().setBirthDate(date);

        var districtsName = gorzdravService.getDistricts().stream()
                .map(District::name)
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(districtsName);

        userState.setHandlerName("AddMedCardDiscrictHandler");
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_DISCRICT)
                .build();
    }

    private Date stringToDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
