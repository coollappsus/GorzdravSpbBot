package com.example.gorzdrav_spb_bot.handler.handlers.cancel;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.MedicalCardContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
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
import java.util.List;
import java.util.Locale;

@Component
@AllArgsConstructor
public class CancelAppointmentChooseHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_HEADER = "📝Выберите запись для отмены";
    private static final String NOT_FOUND_APPOINTMENT_RESPONSE_TEXT = "📝Записей не найдено!";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMMM yyyy, HH:mm", new Locale("ru"));

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final StartHandler startHandler;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        LPU lpu = userState.getLpu();
        MedicalCardContext medicalCard = userState.getMedicalCardContext();
        var appointments = gorzdravService.getFullAppointments(lpu, medicalCard.getPatientId());

        if (appointments == null || appointments.isEmpty()) {
            userState.setHandlerName("StartHandler");
            telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), NOT_FOUND_APPOINTMENT_RESPONSE_TEXT);
            return startHandler.processMessage(message, userState);
        }

        List<FullAppointment> sortedAppointments = appointments.stream()
                .sorted(Comparator.comparing(FullAppointment::visitStart))
                .toList();
        List<String> orderedIds = sortedAppointments.stream()
                .map(FullAppointment::appointmentId)
                .toList();
        userState.setAppointmentIds(orderedIds);

        var infoAboutAppointment = sortedAppointments.stream()
                .map(a -> DATE_FORMAT.format(a.visitStart()) + " к врачу " + a.doctorRendingConsultation().name())
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(infoAboutAppointment);

        userState.setHandlerName("CancelAppointmentAppHandler");
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_HEADER)
                .build();
    }
}
