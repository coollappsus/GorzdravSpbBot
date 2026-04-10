package com.example.gorzdrav_spb_bot.handler.handlers.create;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.vk.KeyboardFactory;
import com.vk.api.sdk.objects.messages.Message;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CreateAppointmentDistrictHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_LPU = "Выберите лечебно профилактическое учреждение";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final CreateAppointmentLpuHandler createAppointmentLpuHandler;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        String districtName = message.getText();
        District district = gorzdravService.getDistricts().stream()
                .filter(d -> d.name().equals(districtName))
                .findFirst().orElseThrow();
        userState.getContext().add(district);

        var lpuName = gorzdravService.getLPUs(district).stream()
                .map(lpu -> lpu.lpuShortName() + " по адресу " + lpu.address())
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(lpuName);

        userState.setHandler(createAppointmentLpuHandler);
        return VkResponse.builder()
                .keyboard(keyboard)
                .message(RESPONSE_TEXT_LPU)
                .build();
    }
}
