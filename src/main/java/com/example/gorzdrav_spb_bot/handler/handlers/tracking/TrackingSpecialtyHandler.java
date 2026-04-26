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

@Component
@AllArgsConstructor
public class TrackingSpecialtyHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_DOCTOR = "Выберите доктора для записи";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final TrackingDoctorHandler trackingDoctorHandler;
    private final ContextUtil contextUtil;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        LPU lpu = contextUtil.getContextObject(userState, LPU.class);
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
        return VkResponse.builder()
                .keyboard(keyboard)
                .message(RESPONSE_TEXT_DOCTOR)
                .build();
    }
}
