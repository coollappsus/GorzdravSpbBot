package com.example.gorzdrav_spb_bot.handler.handlers.tracking;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Specialty;
import com.example.gorzdrav_spb_bot.service.vk.KeyboardFactory;
import api.longpoll.bots.model.objects.basic.Message;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class TrackingDoctorHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_APPOINTMENT = """
            Введите дату в формате dd.mm.yyyy для отслеживания номерков.
            Если необходимо попасть к врачу в любой день, выберите "Не важно".
            """;

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final TrackingDayPreferenceHandler trackingDayPreferenceHandler;
    private final ContextUtil contextUtil;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        LPU lpu = contextUtil.getContextObject(userState, LPU.class);
        Specialty specialty = contextUtil.getContextObject(userState, Specialty.class);
        Doctor doctor = gorzdravService.getDoctors(specialty, lpu).stream()
                .filter(d -> message.getText().equals(d.name().trim()))
                .findFirst()
                .orElseThrow();
        userState.getContext().add(doctor);

        var keyboard = keyboardFactory.createReplyKeyboard(List.of("Не важно"));
        userState.setHandler(trackingDayPreferenceHandler);
        return VkResponse.builder()
                .keyboard(keyboard)
                .message(RESPONSE_TEXT_APPOINTMENT)
                .build();
    }
}
