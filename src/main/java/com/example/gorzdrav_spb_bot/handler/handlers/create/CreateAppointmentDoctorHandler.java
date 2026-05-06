package com.example.gorzdrav_spb_bot.handler.handlers.create;

import api.longpoll.bots.model.objects.basic.Message;
import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Specialty;
import com.example.gorzdrav_spb_bot.service.vk.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@AllArgsConstructor
public class CreateAppointmentDoctorHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_APPOINTMENT = "Выберите день для записи. Перечислены только доступные дни.";
    public static final DateTimeFormatter dayMonthFormatter = DateTimeFormatter.ofPattern("d MMMM", new Locale("ru"));

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final CreateAppointmentDayHandler createAppointmentDayHandler;
    private final ContextUtil contextUtil;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        LPU lpu = contextUtil.getContextObject(userState, LPU.class);
        Specialty specialty = contextUtil.getContextObject(userState, Specialty.class);
        Doctor doctor = gorzdravService.getDoctors(specialty, lpu).stream()
                .filter(d -> message.getText().equals(d.name().trim()))
                .findFirst()
                .orElseThrow();

        var visitDaysString = gorzdravService.getAppointments(lpu, doctor).stream()
                .map(a -> a.visitStart().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate())
                .distinct()
                .sorted()
                .map(dayMonthFormatter::format)
                .toList();

        userState.getContext().add(doctor);
        var keyboard = keyboardFactory.createReplyKeyboard(visitDaysString);
        userState.setHandler(createAppointmentDayHandler);

        return VkResponse.builder()
                .keyboard(keyboard)
                .message(RESPONSE_TEXT_APPOINTMENT)
                .build();
    }
}
