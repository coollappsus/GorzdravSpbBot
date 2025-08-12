package com.example.gorzdrav_spb_bot.handler.handlers.create;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Specialty;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class CreateAppointmentLpuHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_SPECIALTY = "Выберите специалиста для записи";

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final CreateAppointmentSpecialtyHandler createAppointmentSpecialtyHandler;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        District district = userState.getContext().stream()
                .filter(d -> d instanceof District)
                .map(d -> (District) d)
                .findFirst()
                .orElseThrow();
        String lpuName = message.getText().substring(0, message.getText().indexOf(" по адресу"));
        LPU lpu = gorzdravService.getLPUs(district).stream()
                .filter(l -> l.lpuShortName().equals(lpuName))
                .findFirst()
                .orElseThrow();

        var specialtiesName = gorzdravService.getSpecialties(lpu).stream()
                .map(Specialty::name)
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(specialtiesName);
        userState.setHandler(createAppointmentSpecialtyHandler);
        userState.getContext().add(lpu);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_SPECIALTY)
                .build();
    }
}
