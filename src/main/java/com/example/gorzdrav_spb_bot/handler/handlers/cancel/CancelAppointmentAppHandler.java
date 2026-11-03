package com.example.gorzdrav_spb_bot.handler.handlers.cancel;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.MedicalCardContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.cache.util.ContextUtil;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.FullAppointment;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@AllArgsConstructor
public class CancelAppointmentAppHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FINISH_APPOINTMENT = "Поздравляю! Запись к врачу отменена✨";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMMM yyyy, HH:mm", new Locale("ru"));

    private final GorzdravService gorzdravService;
    private final ContextUtil contextUtil;
    private final StartHandler startHandler;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        LPU lpu = userState.getLpu();
        MedicalCardContext medicalCard = userState.getMedicalCardContext();
        var fullAppointments = gorzdravService.getFullAppointments(lpu, medicalCard.getPatientId());

        List<String> buttonTexts = fullAppointments.stream()
                .sorted(Comparator.comparing(FullAppointment::visitStart))
                .map(a -> DATE_FORMAT.format(a.visitStart()) + " к врачу " + a.doctorRendingConsultation().name().trim())
                .toList();
        int index = buttonTexts.indexOf(message.getText().trim());

        if (index == -1) {
            telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), "Пожалуйста, выберите время на клавиатуре.");
            return null;
        }
        String appointmentId = userState.getAppointmentIds().get(index);

        var fullAppointment = fullAppointments.stream()
                .filter(fa -> fa.appointmentId().equals(appointmentId))
                .findFirst()
                .orElseThrow();

        gorzdravService.cancelAppointment(fullAppointment, lpu, medicalCard.getPatientId());
        contextUtil.cleanAllContext(userState);

        telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), RESPONSE_TEXT_FINISH_APPOINTMENT);
        return startHandler.processMessage(message, userState);
    }
}
