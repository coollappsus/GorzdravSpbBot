package com.example.gorzdrav_spb_bot.service;

import com.example.gorzdrav_spb_bot.model.User;
import com.example.gorzdrav_spb_bot.repository.UserRepository;
import com.example.gorzdrav_spb_bot.service.vk.VkAsyncMessageSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.gorzdrav_spb_bot.config.Const.ADMIN_ID;

@Component
@AllArgsConstructor
@Slf4j
public class UserChecker {

    private final UserRepository userRepository;
    private final VkAsyncMessageSender vkAsyncMessageSender;

    @Scheduled(fixedDelay = 3600000) //Раз в 1 час
    public void checkNewUser() {
        log.info("Checking new user");
        List<User> newUsers = userRepository.getNewUsers();

        if (newUsers.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append(newUsers.size()).append("🎉🎉New users found🎉🎉\n");
        for (User user : newUsers) {
            sb.append(user.getUserName()).append("\n");
        }

        log.info("notify admin about new users");
        vkAsyncMessageSender.sendMessageToUser(ADMIN_ID, sb.toString());
    }
}
