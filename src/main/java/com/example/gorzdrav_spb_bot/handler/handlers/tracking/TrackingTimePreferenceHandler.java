package com.example.gorzdrav_spb_bot.handler.handlers.tracking;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.model.Task;
import com.example.gorzdrav_spb_bot.model.TimePreference;
import com.example.gorzdrav_spb_bot.model.User;
import com.example.gorzdrav_spb_bot.repository.TaskRepository;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;

@Component
public class TrackingTimePreferenceHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FINISH_TRACKING = "Поздравляю! Задача на отслеживание талончиков к врачу создана✨";

    private final TaskRepository taskRepository;
    private final ContextUtil contextUtil;
    private final StartHandler startHandler;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    public TrackingTimePreferenceHandler(TaskRepository taskRepository, ContextUtil contextUtil,
                                         @Lazy StartHandler startHandler,
                                         TelegramAsyncMessageSender telegramAsyncMessageSender) {
        this.taskRepository = taskRepository;
        this.contextUtil = contextUtil;
        this.startHandler = startHandler;
        this.telegramAsyncMessageSender = telegramAsyncMessageSender;
    }

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        TimePreference timePreference = Arrays.stream(TimePreference.values())
                .filter(tp -> tp.getAdditionalName().equals(message.getText()))
                .findFirst()
                .orElseThrow();
        userState.getContext().add(timePreference);
        LPU lpu = userState.getContext().stream()
                .filter(l -> l instanceof LPU)
                .map(LPU.class::cast)
                .findFirst()
                .orElseThrow();
        MedicalCard medicalCard = userState.getContext().stream()
                .filter(mc -> mc instanceof MedicalCard)
                .map(MedicalCard.class::cast)
                .findFirst()
                .orElseThrow();
        User user = userState.getContext().stream()
                .filter(u -> u instanceof User)
                .map(User.class::cast)
                .findFirst()
                .orElseThrow();
        Doctor doctor = userState.getContext().stream()
                .filter(d -> d instanceof Doctor)
                .map(Doctor.class::cast)
                .findFirst()
                .orElseThrow();

        taskRepository.save(Task.builder()
                .timePreference(timePreference)
                .lpuId(lpu.id())
                .owner(user)
                .medicalCard(medicalCard)
                .doctorId(doctor.id())
                .activeStatus(true)
                .build());
        userState.setHandler(startHandler);
        contextUtil.cleanAllContext(userState);
        telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), RESPONSE_TEXT_FINISH_TRACKING);

        return startHandler.processMessage(message, userState);
    }
}
