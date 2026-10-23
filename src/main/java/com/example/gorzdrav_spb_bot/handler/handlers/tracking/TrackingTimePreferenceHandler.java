package com.example.gorzdrav_spb_bot.handler.handlers.tracking;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.cache.util.ContextUtil;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.model.Task;
import com.example.gorzdrav_spb_bot.model.TimePreference;
import com.example.gorzdrav_spb_bot.model.User;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import com.example.gorzdrav_spb_bot.repository.TaskRepository;
import com.example.gorzdrav_spb_bot.repository.UserRepository;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.Date;

@Component
@AllArgsConstructor
public class TrackingTimePreferenceHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FINISH_TRACKING = "Поздравляю! Задача №%s на отслеживание талончиков к врачу создана✨";

    private final MedicalCardRepository medicalCardRepository;
    private final TaskRepository taskRepository;
    private final ContextUtil contextUtil;
    private final UserRepository userRepository;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;
    private final StartHandler startHandler;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        TimePreference timePreference = Arrays.stream(TimePreference.values())
                .filter(tp -> message.getText().equals(tp.getAdditionalName().trim()))
                .findFirst()
                .orElseThrow();
        userState.setTimePreference(timePreference);
        LPU lpu = userState.getLpu();
        User user = userRepository.findUserByUserId(userState.getUserContext().userId());
        MedicalCard medicalCard = medicalCardRepository.findById(userState.getMedicalCardContext().getId()).orElseThrow();
        Doctor doctor = userState.getDoctor();
        Date preferenceDate = getPreferenceDate(userState);

        Long taskId = taskRepository.save(Task.builder()
                .timePreference(timePreference)
                .lpuId(lpu.id())
                .owner(user)
                .medicalCard(medicalCard)
                .doctorId(doctor.id())
                .activeStatus(true)
                .preferenceDate(preferenceDate)
                .build()).getId();

        contextUtil.cleanAllContext(userState);
        telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), RESPONSE_TEXT_FINISH_TRACKING.formatted(taskId));
        return startHandler.processMessage(message, userState);
    }

    private Date getPreferenceDate(UserState userState) {
        Date preferenceDate;
        try {
            preferenceDate = userState.getPreferedDate();
        } catch (Exception e) {
            preferenceDate = null;
        }
        return preferenceDate;
    }
}
