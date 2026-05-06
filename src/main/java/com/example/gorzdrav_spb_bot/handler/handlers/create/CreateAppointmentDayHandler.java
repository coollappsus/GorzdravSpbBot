package com.example.gorzdrav_spb_bot.handler.handlers.create;

import api.longpoll.bots.model.objects.basic.Message;
import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.SelectedAppointmentDay;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Appointment;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.vk.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;

@Component
@AllArgsConstructor
public class CreateAppointmentDayHandler implements VkUpdateMessageHandler {


    private static final String RESPONSE_TEXT_APPOINTMENT = "Выберите время для записи";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM", new Locale("ru"));
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", new Locale("ru"));


    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final CreateAppointmentChooseAppHandler createAppointmentChooseAppHandler;
    private final ContextUtil contextUtil;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        LPU lpu = contextUtil.getContextObject(userState, LPU.class);
        Doctor doctor = contextUtil.getContextObject(userState, Doctor.class);
        String selectedDay = message.getText();

        var allAppointments = gorzdravService.getAppointments(lpu, doctor);

        LocalDate actualDate = allAppointments.stream()
                .map(a -> a.visitStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .filter(date -> DATE_TIME_FORMATTER.format(date).equals(selectedDay))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Дата не найдена"));
        userState.getContext().add(new SelectedAppointmentDay(actualDate, selectedDay));

        var availableTimes = allAppointments.stream()
                .filter(a -> a.visitStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(actualDate))
                .sorted(Comparator.comparing(Appointment::visitStart))
                .map(a -> TIME_FORMATTER.format(a.visitStart().toInstant().atZone(ZoneId.systemDefault()))
                        + " - " +
                        TIME_FORMATTER.format(a.visitEnd().toInstant().atZone(ZoneId.systemDefault())))
                .toList();

        var keyboard = keyboardFactory.createReplyKeyboard(availableTimes);
        userState.setHandler(createAppointmentChooseAppHandler);

        return VkResponse.builder()
                .keyboard(keyboard)
                .message(RESPONSE_TEXT_APPOINTMENT)
                .build();
    }
}
