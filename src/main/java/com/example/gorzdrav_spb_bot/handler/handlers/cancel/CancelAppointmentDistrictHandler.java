package com.example.gorzdrav_spb_bot.handler.handlers.cancel;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class CancelAppointmentDistrictHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_LPU = "Выберите лечебно профилактическое учреждение";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final CancelAppointmentLpuHandler cancelAppointmentLpuHandler;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        String districtName = message.getText();
        District district = gorzdravService.getDistricts().stream()
                .filter(d -> d.name().equals(districtName))
                .findFirst().orElseThrow();
        userState.getContext().add(district);

        var lpuName = gorzdravService.getLPUs(district).stream()
                .map(lpu -> lpu.lpuShortName() + " по адресу " + lpu.address())
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(lpuName);

        userState.setHandler(cancelAppointmentLpuHandler);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_LPU)
                .build();
    }
}
