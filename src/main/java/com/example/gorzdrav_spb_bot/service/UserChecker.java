package com.example.gorzdrav_spb_bot.service;

import com.example.gorzdrav_spb_bot.model.User;
import com.example.gorzdrav_spb_bot.repository.UserRepository;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class UserChecker {

    private final static Long ADMIN_ID = 906044021L;

    private final UserRepository userRepository;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    @Scheduled(fixedDelay = 3600000) //Раз в 1 час
    public void checkNewUser() {
        List<User> newUsers = userRepository.getNewUsers();

        if (newUsers.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append(newUsers.size()).append("🎉🎉New users found🎉🎉\n");
        for (User user : newUsers) {
            sb.append(user.getUserName()).append("\n");
        }

        telegramAsyncMessageSender.sendMessageToUser(ADMIN_ID, sb.toString());
    }
}
