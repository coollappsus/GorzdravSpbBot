package com.example.gorzdrav_spb_bot.handler.handlers.find;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.vk.KeyboardFactory;
import api.longpoll.bots.model.objects.basic.Message;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class FindAppointmentDistrictHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_LPU_TYPE = "Выберите тип лечебно профилактического учреждения";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final FindApointmentLpuTypeHandler findApointmentLpuTypeHandler;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        String districtName = message.getText();
        District district = gorzdravService.getDistricts().stream()
                .filter(d -> d.name().equals(districtName))
                .findFirst().orElseThrow();
        userState.getContext().add(district);

        var lpuTypes = gorzdravService.getLPUs(district).stream()
                .map(LPU::lpuType)
                .collect(Collectors.toSet());
        var keyboard = keyboardFactory.createReplyKeyboard(lpuTypes);

        userState.setHandler(findApointmentLpuTypeHandler);
        return VkResponse.builder()
                .keyboard(keyboard)
                .message(RESPONSE_TEXT_LPU_TYPE)
                .build();
    }
}
