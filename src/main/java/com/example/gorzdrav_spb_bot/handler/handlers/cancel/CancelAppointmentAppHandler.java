package com.example.gorzdrav_spb_bot.handler.handlers.cancel;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class CancelAppointmentAppHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FINISH_APPOINTMENT = "Поздравляю! Запись к врачу отменена✨";

    private final GorzdravService gorzdravService;
    private final StartHandler startHandler;
    private final ContextUtil contextUtil;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    public CancelAppointmentAppHandler(GorzdravService gorzdravService, @Lazy StartHandler startHandler,
                                       ContextUtil contextUtil, TelegramAsyncMessageSender telegramAsyncMessageSender) {
        this.gorzdravService = gorzdravService;
        this.startHandler = startHandler;
        this.contextUtil = contextUtil;
        this.telegramAsyncMessageSender = telegramAsyncMessageSender;
    }

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
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
        telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), RESPONSE_TEXT_FINISH_APPOINTMENT);
        return startHandler.processMessage(message, userState);
    }
}
