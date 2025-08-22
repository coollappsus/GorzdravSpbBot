package com.example.gorzdrav_spb_bot.handler.handlers.create;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Appointment;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.CONFIRMATION;

@Component
public class CreateAppointmentConfirmationHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FINISH_APPOINTMENT = "Поздравляю! Запись к врачу создана✨";

    private final GorzdravService gorzdravService;
    private final StartHandler startHandler;
    private final ContextUtil contextUtil;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    public CreateAppointmentConfirmationHandler(GorzdravService gorzdravService, @Lazy StartHandler startHandler,
                                                ContextUtil contextUtil,
                                                TelegramAsyncMessageSender telegramAsyncMessageSender) {
        this.gorzdravService = gorzdravService;
        this.startHandler = startHandler;
        this.contextUtil = contextUtil;
        this.telegramAsyncMessageSender = telegramAsyncMessageSender;
    }

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        if (message.getText().equals(CONFIRMATION.getText())) {
            LPU lpu = contextUtil.getContextObject(userState, LPU.class);
            Appointment appointment = contextUtil.getContextObject(userState, Appointment.class);
            MedicalCard medicalCard = contextUtil.getContextObject(userState, MedicalCard.class);
            gorzdravService.createAppointment(appointment, lpu, medicalCard.getPatientId());

            telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), RESPONSE_TEXT_FINISH_APPOINTMENT);
        }
        userState.setHandler(startHandler);
        contextUtil.cleanAllContext(userState);

        return startHandler.processMessage(message, userState);
    }
}
