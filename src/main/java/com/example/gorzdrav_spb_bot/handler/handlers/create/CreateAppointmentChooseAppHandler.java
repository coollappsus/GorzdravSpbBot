package com.example.gorzdrav_spb_bot.handler.handlers.create;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Appointment;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.CONFIRMATION;
import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.TO_MAIN;

@Component
@AllArgsConstructor
public class CreateAppointmentChooseAppHandler implements TelegramUpdateMessageHandler {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMMM yyyy, HH:mm");
    private static final String RESPONSE_TEXT_CONFIRMATION = """
            Подтвердите выбранные данные.
            🚑Лечебно-профилактическое учреждение - %s
            👩‍⚕️Доктор - %s
            ⏱Время - %s
            👤ФИО пациента - %s
            """;

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final CreateAppointmentConfirmationHandler createAppointmentConfirmationHandler;
    private final ContextUtil contextUtil;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        LPU lpu = contextUtil.getContextObject(userState, LPU.class);
        Doctor doctor = contextUtil.getContextObject(userState, Doctor.class);

        String appointmentId = message.getText().substring(0, message.getText().indexOf(". "));
        Appointment appointment = gorzdravService.getAppointments(lpu, doctor).stream()
                .filter(a -> a.id().equals(appointmentId))
                .findFirst()
                .orElseThrow();
        userState.getContext().add(appointment);
        MedicalCard medicalCard = contextUtil.getContextObject(userState, MedicalCard.class);

        String response = RESPONSE_TEXT_CONFIRMATION.formatted(lpu.lpuShortName(), doctor.name(),
                DATE_FORMAT.format(appointment.visitStart()) + " - " + DATE_FORMAT.format(appointment.visitEnd()),
                medicalCard.getLastName() + " " + medicalCard.getFirstName() + " " + medicalCard.getMiddleName());
        var keyboard = keyboardFactory.createReplyKeyboard(List.of(TO_MAIN.getText(), CONFIRMATION.getText()));
        userState.setHandler(createAppointmentConfirmationHandler);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(response)
                .build();
    }
}
