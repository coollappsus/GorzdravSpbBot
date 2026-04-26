package com.example.gorzdrav_spb_bot.handler.handlers.find;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.District;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.FullAppointment;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.vk.VkAsyncMessageSender;
import api.longpoll.bots.model.objects.basic.Message;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;

@Component
public class FindAppointmentLpuHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_HEADER = "📝Текущие записи:\n";
    private static final String NOT_FOUND_APPOINTMENT_RESPONSE_TEXT = "📝Записей не найдено!";

    private final GorzdravService gorzdravService;
    private final StartHandler startHandler;
    private final ContextUtil contextUtil;
    private final VkAsyncMessageSender vkAsyncMessageSender;

    public FindAppointmentLpuHandler(GorzdravService gorzdravService, @Lazy StartHandler startHandler,
                                     ContextUtil contextUtil,
                                     VkAsyncMessageSender vkAsyncMessageSender) {
        this.gorzdravService = gorzdravService;
        this.startHandler = startHandler;
        this.contextUtil = contextUtil;
        this.vkAsyncMessageSender = vkAsyncMessageSender;
    }

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        District district = contextUtil.getContextObject(userState, District.class);
        String lpuName = message.getText();
        LPU lpu = gorzdravService.getLPUs(district).stream()
                .filter(l -> l.lpuShortName().equals(lpuName))
                .findFirst()
                .orElseThrow();
        userState.getContext().add(lpu);

        MedicalCard medicalCard = userState.getContext().stream()
                .filter(mc -> mc instanceof MedicalCard)
                .map(mc -> (MedicalCard) mc)
                .findFirst()
                .orElseThrow();
        var appointments = gorzdravService.getFullAppointments(lpu, medicalCard.getPatientId());

        userState.setHandler(startHandler);
        contextUtil.cleanAllContext(userState);

        if (appointments == null || appointments.isEmpty()) {
            vkAsyncMessageSender.sendMessageToUser(Long.valueOf(message.getFromId()), NOT_FOUND_APPOINTMENT_RESPONSE_TEXT);
        } else {
            vkAsyncMessageSender.sendMessageToUser(Long.valueOf(message.getFromId()), creationResponseText(appointments));
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
                    .append("⏱Время - ")
                    .append(dateString).append("\n\n");
        }

        return sb.toString();
    }
}
