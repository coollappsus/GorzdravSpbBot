package com.example.gorzdrav_spb_bot.handler.handlers.tracking;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.create.CreateAppointmentDoctorHandler;
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

@Component
@AllArgsConstructor
public class TrackingSpecialtyHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_DOCTOR = "Выберите доктора для записи";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final TrackingDoctorHandler trackingDoctorHandler;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        LPU lpu = userState.getContext().stream()
                .filter(l -> l instanceof LPU)
                .map(l -> (LPU) l)
                .findFirst()
                .orElseThrow();
        String specialtyName = message.getText();
        Specialty specialty = gorzdravService.getSpecialties(lpu).stream()
                .filter(s -> s.name().equals(specialtyName))
                .findFirst()
                .orElseThrow();
        userState.getContext().add(specialty);

        var doctorsName = gorzdravService.getDoctors(specialty, lpu).stream()
                .map(Doctor::name)
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(doctorsName);
        userState.setHandler(trackingDoctorHandler);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_DOCTOR)
                .build();
    }
}
