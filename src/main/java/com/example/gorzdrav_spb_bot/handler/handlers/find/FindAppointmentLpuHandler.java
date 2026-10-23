package com.example.gorzdrav_spb_bot.handler.handlers.find;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.MedicalCardContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.cache.util.ContextUtil;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.FullAppointment;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.text.SimpleDateFormat;
import java.util.List;

@Component
@AllArgsConstructor
public class FindAppointmentLpuHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_HEADER = "📝Текущие записи:\n";
    private static final String NOT_FOUND_APPOINTMENT_RESPONSE_TEXT = "📝Записей не найдено!";

    private final GorzdravService gorzdravService;
    private final StartHandler startHandler;
    private final ContextUtil contextUtil;
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

        contextUtil.cleanAllContext(userState);

        if (appointments == null || appointments.isEmpty()) {
            telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), NOT_FOUND_APPOINTMENT_RESPONSE_TEXT);
        } else {
            telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), creationResponseText(appointments));
        }
        return startHandler.processMessage(message, userState);
    }

    private String creationResponseText(List<FullAppointment> appointments) {
        StringBuilder sb = new StringBuilder();
        sb.append(RESPONSE_TEXT_HEADER);

        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm");
        for (int i = 0; i < appointments.size(); i++) {
            String dateString = dateFormat.format(appointments.get(i).visitStart());

            sb.append(i + 1).append(".\n")
                    .append(" \uD83D\uDE91Лечебно-профилактическое учреждение - ")
                    .append(appointments.get(i).lpuShortName()).append("\n")
                    .append("\uD83D\uDC69\u200D⚕\uFE0FДоктор - ")
                    .append(appointments.get(i).doctorRendingConsultation().name()).append("\n")
                    .append("🩺Специальность - ")
                    .append(appointments.get(i).specialityRendingConsultation().name()).append("\n")
                    .append("⏱Время - ")
                    .append(dateString).append("\n\n");
        }

        return sb.toString();
    }
}
