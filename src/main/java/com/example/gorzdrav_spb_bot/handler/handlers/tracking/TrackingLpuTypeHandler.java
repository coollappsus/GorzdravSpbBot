package com.example.gorzdrav_spb_bot.handler.handlers.tracking;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.vk.KeyboardFactory;
import api.longpoll.bots.model.objects.basic.Message;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class TrackingLpuTypeHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_LPU = "Выберите лечебно профилактическое учреждение";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final TrackingLpuHandler trackingLpuHandler;
    private final ContextUtil contextUtil;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        String lpuType = message.getText().trim();
        District district = contextUtil.getContextObject(userState, District.class);

        var lpuNames = gorzdravService.getLPUs(district).stream()
                .filter(lpu -> lpuType.equals(lpu.lpuType().trim()))
                .map(LPU::lpuShortName)
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(lpuNames);

        userState.setHandler(trackingLpuHandler);
        return VkResponse.builder()
                .keyboard(keyboard)
                .message(RESPONSE_TEXT_LPU)
                .build();
    }
}
