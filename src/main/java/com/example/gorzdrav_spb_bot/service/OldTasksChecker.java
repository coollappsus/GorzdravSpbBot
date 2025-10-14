package com.example.gorzdrav_spb_bot.service;

import com.example.gorzdrav_spb_bot.model.Task;
import com.example.gorzdrav_spb_bot.repository.TaskRepository;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class OldTasksChecker {

    private final static Long ADMIN_ID = 906044021L;
    private final static String MESSAGE_TEXT = "OLD TASKS SUCCEEDED REMOVED. COUNT - %s";

    private final TaskRepository taskRepository;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    @Scheduled(cron = "0 5 0 * * *", zone = "Europe/Moscow")
    public void checkAndRemoveOldTasks() {
        log.info("Checking old tasks");
        List<Task> oldTasks = taskRepository.findByPreferenceDateBeforeAndCompleteStatusAndActiveStatus(new Date(),
                false, true);
        if (oldTasks.isEmpty()) {
            log.info("No old tasks found");
            return;
        }

        for (Task task : oldTasks) {
            task.setActiveStatus(false);
            taskRepository.save(task);
        }

        log.info("Saving old tasks and notify admin");
        telegramAsyncMessageSender.sendMessageToUser(ADMIN_ID, MESSAGE_TEXT.formatted(oldTasks.size()));
    }
}
