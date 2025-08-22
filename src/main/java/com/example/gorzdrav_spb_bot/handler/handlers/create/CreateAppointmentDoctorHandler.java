package com.example.gorzdrav_spb_bot.handler.handlers.create;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
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

@Component
@AllArgsConstructor
public class CreateAppointmentDoctorHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_APPOINTMENT = "Выберите время для записи";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final CreateAppointmentChooseAppHandler createAppointmentChooseAppHandler;
    private final ContextUtil contextUtil;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        LPU lpu = contextUtil.getContextObject(userState, LPU.class);
        Specialty specialty = contextUtil.getContextObject(userState, Specialty.class);
        Doctor doctor = gorzdravService.getDoctors(specialty, lpu).stream()
                .filter(d -> d.name().equals(message.getText()))
                .findFirst()
                .orElseThrow();

        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm");
        var visitString = gorzdravService.getAppointments(lpu, doctor).stream()
                .sorted(Comparator.comparing(Appointment::visitStart))
                .map(a -> a.id() + ". " + dateFormat.format(a.visitStart()) + " - " + dateFormat.format(a.visitEnd()))
                .toList();

        userState.getContext().add(doctor);
        var keyboard = keyboardFactory.createReplyKeyboard(visitString);
        userState.setHandler(createAppointmentChooseAppHandler);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_APPOINTMENT)
                .build();
    }
}
