package com.example.gorzdrav_spb_bot.handler.handlers.tracking;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.model.Task;
import com.example.gorzdrav_spb_bot.model.TimePreference;
import com.example.gorzdrav_spb_bot.model.User;
import com.example.gorzdrav_spb_bot.repository.TaskRepository;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.vk.VkAsyncMessageSender;
import api.longpoll.bots.model.objects.basic.Message;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

@Component
public class TrackingTimePreferenceHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FINISH_TRACKING = "Поздравляю! Задача №%s на отслеживание талончиков к врачу создана✨";

    private final TaskRepository taskRepository;
    private final ContextUtil contextUtil;
    private final StartHandler startHandler;
    private final VkAsyncMessageSender vkAsyncMessageSender;

    public TrackingTimePreferenceHandler(TaskRepository taskRepository, ContextUtil contextUtil,
                                         @Lazy StartHandler startHandler,
                                         VkAsyncMessageSender vkAsyncMessageSender) {
        this.taskRepository = taskRepository;
        this.contextUtil = contextUtil;
        this.startHandler = startHandler;
        this.vkAsyncMessageSender = vkAsyncMessageSender;
    }

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        TimePreference timePreference = Arrays.stream(TimePreference.values())
                .filter(tp -> message.getText().equals(tp.getAdditionalName().trim()))
                .findFirst()
                .orElseThrow();
        userState.getContext().add(timePreference);
        LPU lpu = contextUtil.getContextObject(userState, LPU.class);
        MedicalCard medicalCard = contextUtil.getContextObject(userState, MedicalCard.class);
        User user = contextUtil.getContextObject(userState, User.class);
        Doctor doctor = contextUtil.getContextObject(userState, Doctor.class);
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
        userState.setHandler(startHandler);
        contextUtil.cleanAllContext(userState);
        vkAsyncMessageSender.sendMessageToUser(Long.valueOf(message.getFromId()), RESPONSE_TEXT_FINISH_TRACKING.formatted(taskId));

        return startHandler.processMessage(message, userState);
    }

    private Date getPreferenceDate(UserState userState) {
        Date preferenceDate;
        try {
            preferenceDate = contextUtil.getContextObject(userState, Date.class);
        } catch (Exception e) {
            preferenceDate = null;
        }
        return preferenceDate;
    }
}
