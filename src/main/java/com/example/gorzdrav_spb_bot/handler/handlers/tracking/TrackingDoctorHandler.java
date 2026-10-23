package com.example.gorzdrav_spb_bot.handler.handlers.tracking;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
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

import java.util.List;

@Component
@AllArgsConstructor
public class TrackingDoctorHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_APPOINTMENT = """
            Введите дату в формате dd.mm.yyyy для отслеживания номерков.
            Если необходимо попасть к врачу в любой день, выберите "Не важно".
            """;

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        LPU lpu = userState.getLpu();
        Specialty specialty = userState.getSpecialty();
        Doctor doctor = gorzdravService.getDoctors(specialty, lpu).stream()
                .filter(d -> message.getText().equals(d.name().trim()))
                .findFirst()
                .orElseThrow();
        userState.setDoctor(doctor);

        var keyboard = keyboardFactory.createReplyKeyboard(List.of("Не важно"));
        userState.setHandlerName("TrackingDayPreferenceHandler");
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_APPOINTMENT)
                .build();
    }
}
