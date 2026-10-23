package com.example.gorzdrav_spb_bot.handler.handlers.cancel;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.MedicalCardContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.FullAppointment;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.text.SimpleDateFormat;
import java.util.Comparator;

@Component
@AllArgsConstructor
public class CancelAppointmentLpuHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_HEADER = "📝Выберите запись для отмены";
    private static final String NOT_FOUND_APPOINTMENT_RESPONSE_TEXT = "📝Записей не найдено!";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMMM yyyy, HH:mm");

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final StartHandler startHandler;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        District district = userState.getDistrict();
        String lpuName = message.getText().substring(0, message.getText().indexOf(" по адресу"));
        LPU lpu = gorzdravService.getLPUs(district).stream()
                .filter(l -> l.lpuShortName().equals(lpuName))
                .findFirst()
                .orElseThrow();
        userState.setLpu(lpu);

        MedicalCardContext medicalCard = userState.getMedicalCardContext();
        var appointments = gorzdravService.getFullAppointments(lpu, medicalCard.getPatientId());

        if (appointments == null || appointments.isEmpty()) {
            userState.setHandlerName("StartHandler");
            telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), NOT_FOUND_APPOINTMENT_RESPONSE_TEXT);
            return startHandler.processMessage(message, userState);
        }

        var visitStartStringList = appointments.stream()
                .sorted(Comparator.comparing(FullAppointment::visitStart))
                .map(a -> a.appointmentId() + ". " + DATE_FORMAT.format(a.visitStart()) )
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(visitStartStringList);
        userState.setHandlerName("CancelAppointmentAppHandler");
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_HEADER)
                .build();
    }
}
