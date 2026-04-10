package com.example.gorzdrav_spb_bot.handler.handlers.cancel;

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
import com.example.gorzdrav_spb_bot.service.vk.KeyboardFactory;
import com.example.gorzdrav_spb_bot.service.vk.VkAsyncMessageSender;
import com.vk.api.sdk.objects.messages.Message;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Comparator;

@Component
public class CancelAppointmentLpuHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_HEADER = "📝Выберите запись для отмены";
    private static final String NOT_FOUND_APPOINTMENT_RESPONSE_TEXT = "📝Записей не найдено!";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm");

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final CancelAppointmentAppHandler cancelAppointmentAppHandler;
    private final StartHandler startHandler;
    private final VkAsyncMessageSender vkAsyncMessageSender;
    private final ContextUtil contextUtil;

    public CancelAppointmentLpuHandler(GorzdravService gorzdravService, KeyboardFactory keyboardFactory,
                                       CancelAppointmentAppHandler cancelAppointmentAppHandler,
                                       @Lazy StartHandler startHandler,
                                       VkAsyncMessageSender vkAsyncMessageSender, ContextUtil contextUtil) {
        this.gorzdravService = gorzdravService;
        this.keyboardFactory = keyboardFactory;
        this.cancelAppointmentAppHandler = cancelAppointmentAppHandler;
        this.startHandler = startHandler;
        this.vkAsyncMessageSender = vkAsyncMessageSender;
        this.contextUtil = contextUtil;
    }

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        District district = contextUtil.getContextObject(userState, District.class);
        String lpuName = message.getText().substring(0, message.getText().indexOf(" по адресу"));
        LPU lpu = gorzdravService.getLPUs(district).stream()
                .filter(l -> l.lpuShortName().equals(lpuName))
                .findFirst()
                .orElseThrow();
        userState.getContext().add(lpu);

        MedicalCard medicalCard = contextUtil.getContextObject(userState, MedicalCard.class);
        var appointments = gorzdravService.getFullAppointments(lpu, medicalCard.getPatientId());

        if (appointments == null || appointments.isEmpty()) {
            userState.setHandler(startHandler);
            vkAsyncMessageSender.sendMessageToUser(message.getFromId(), NOT_FOUND_APPOINTMENT_RESPONSE_TEXT);
            return startHandler.processMessage(message, userState);
        }

        var visitStartStringList = appointments.stream()
                .sorted(Comparator.comparing(FullAppointment::visitStart))
                .map(a -> a.appointmentId() + ". " + dateFormat.format(a.visitStart()) )
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(visitStartStringList);
        userState.setHandler(cancelAppointmentAppHandler);
        return VkResponse.builder()
                .keyboard(keyboard)
                .message(RESPONSE_TEXT_HEADER)
                .build();
    }
}
