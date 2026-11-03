package com.example.gorzdrav_spb_bot.handler.handlers.create;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Appointment;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Specialty;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@AllArgsConstructor
public class CreateAppointmentDoctorHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_APPOINTMENT = "Выберите время для записи";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMMM yyyy, HH:mm", new Locale("ru"));

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

        List<Appointment> sortedAppointments = gorzdravService.getAppointments(lpu, doctor).stream()
                .sorted(Comparator.comparing(Appointment::visitStart))
                .toList();
        List<String> orderedIds = sortedAppointments.stream()
                .map(Appointment::id)
                .toList();
        userState.setAppointmentIds(orderedIds);

        var visitString = sortedAppointments.stream()
                .map(a -> DATE_FORMAT.format(a.visitStart()) + " - " + DATE_FORMAT.format(a.visitEnd()))
                .toList();

        userState.setDoctor(doctor);
        var keyboard = keyboardFactory.createReplyKeyboard(visitString);
        userState.setHandlerName("CreateAppointmentChooseAppHandler");
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_APPOINTMENT)
                .build();
    }
}
