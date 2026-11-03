package com.example.gorzdrav_spb_bot.handler.handlers.create;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.MedicalCardContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Appointment;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
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

import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.CONFIRMATION;

@Component
@AllArgsConstructor
public class CreateAppointmentChooseAppHandler implements TelegramUpdateMessageHandler {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMMM yyyy, HH:mm", new Locale("ru"));
    private static final String RESPONSE_TEXT_CONFIRMATION = """
            Подтвердите выбранные данные.
            🚑Лечебно-профилактическое учреждение - %s
            👩‍⚕️Доктор - %s
            ⏱Время - %s
            👤ФИО пациента - %s
            """;

    private final GorzdravService gorzdravService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        LPU lpu = userState.getLpu();
        Doctor doctor = userState.getDoctor();
        var appointments = gorzdravService.getAppointments(lpu, doctor);

        List<String> buttonTexts = appointments.stream()
                .sorted(Comparator.comparing(Appointment::visitStart))
                .map(a -> DATE_FORMAT.format(a.visitStart()) + " - " + DATE_FORMAT.format(a.visitEnd()))
                .toList();
        int index = buttonTexts.indexOf(message.getText());
        if (index == -1) {
            telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), "Пожалуйста, выберите время на клавиатуре.");
            return null;
        }
        String appointmentId = userState.getAppointmentIds().get(index);
        Appointment appointment = appointments.stream()
                .filter(a -> a.id().equals(appointmentId))
                .findFirst()
                .orElseThrow();
        userState.setAppointment(appointment);
        MedicalCardContext medicalCard = userState.getMedicalCardContext();

        String response = RESPONSE_TEXT_CONFIRMATION.formatted(lpu.lpuShortName(), doctor.name(),
                DATE_FORMAT.format(appointment.visitStart()) + " - " + DATE_FORMAT.format(appointment.visitEnd()),
                medicalCard.getLastName() + " " + medicalCard.getFirstName() + " " + medicalCard.getMiddleName());
        var keyboard = keyboardFactory.createReplyKeyboard(List.of(CONFIRMATION.getText()));
        userState.setHandlerName("CreateAppointmentConfirmationHandler");
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(response)
                .build();
    }
}
