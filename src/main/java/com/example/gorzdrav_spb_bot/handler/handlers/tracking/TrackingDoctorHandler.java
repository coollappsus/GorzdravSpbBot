package com.example.gorzdrav_spb_bot.handler.handlers.tracking;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.model.TimePreference;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Specialty;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class TrackingDoctorHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_APPOINTMENT = """
            Выберите предпочитаемое время для записи.
            Утро(с 8:00 до 12:00)
            День(С 12:00 до 17:00)
            Вечер(с 17:00 до 20:00)
            Если талона с предпочитаемым временем не будет найдено, будет произведена запись на любое свободное время.
            """;

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final TrackingTimePreferenceHandler trackingTimePreferenceHandler;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        LPU lpu = userState.getContext().stream()
                .filter(l -> l instanceof LPU)
                .map(l -> (LPU) l)
                .findFirst()
                .orElseThrow();
        Specialty specialty = userState.getContext().stream()
                .filter(s -> s instanceof Specialty)
                .map(s -> (Specialty) s)
                .findFirst()
                .orElseThrow();
        Doctor doctor = gorzdravService.getDoctors(specialty, lpu).stream()
                .filter(d -> d.name().equals(message.getText()))
                .findFirst()
                .orElseThrow();
        userState.getContext().add(doctor);


        List<String> preferenceTimeStrings = Arrays.stream(TimePreference.values())
                .map(TimePreference::getAdditionalName)
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(preferenceTimeStrings);
        userState.setHandler(trackingTimePreferenceHandler);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_APPOINTMENT)
                .build();
    }
}
