package com.example.gorzdrav_spb_bot.handler.handlers.cancel;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.vk.VkAsyncMessageSender;
import api.longpoll.bots.model.objects.basic.Message;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CancelAppointmentAppHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FINISH_APPOINTMENT = "Поздравляю! Запись к врачу отменена✨";

    private final GorzdravService gorzdravService;
    private final StartHandler startHandler;
    private final ContextUtil contextUtil;
    private final VkAsyncMessageSender vkAsyncMessageSender;

    public CancelAppointmentAppHandler(GorzdravService gorzdravService, @Lazy StartHandler startHandler,
                                       ContextUtil contextUtil, VkAsyncMessageSender vkAsyncMessageSender) {
        this.gorzdravService = gorzdravService;
        this.startHandler = startHandler;
        this.contextUtil = contextUtil;
        this.vkAsyncMessageSender = vkAsyncMessageSender;
    }

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        LPU lpu = contextUtil.getContextObject(userState, LPU.class);
        MedicalCard medicalCard = contextUtil.getContextObject(userState, MedicalCard.class);
        String appointmentId = message.getText().substring(0, message.getText().indexOf(". "));
        var fullAppointment = gorzdravService.getFullAppointments(lpu, medicalCard.getPatientId()).stream()
                .filter(fa -> fa.appointmentId().equals(appointmentId))
                .findFirst()
                .orElseThrow();

        gorzdravService.cancelAppointment(fullAppointment, lpu, medicalCard.getPatientId());
        userState.setHandler(startHandler);
        contextUtil.cleanAllContext(userState);
        vkAsyncMessageSender.sendMessageToUser(Long.valueOf(message.getFromId()), RESPONSE_TEXT_FINISH_APPOINTMENT);
        return startHandler.processMessage(message, userState);
    }
}
