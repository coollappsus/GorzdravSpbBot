package com.example.gorzdrav_spb_bot.handler.handlers;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.model.User;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import com.example.gorzdrav_spb_bot.repository.UserRepository;
import com.example.gorzdrav_spb_bot.service.vk.KeyboardFactory;
import com.example.gorzdrav_spb_bot.service.vk.VkUsersService;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.ADD;

@Component
@AllArgsConstructor
public class StartHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT = "Выберите мед.карту или добавьте новую";

    private final MedicalCardRepository medicalCardRepository;
    private final UserRepository userRepository;
    private final KeyboardFactory keyboardFactory;
    private final VkUpdateMessageHandler afterStartHandler;
    private final VkUsersService vkUsersService;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
        performNextState(userState);
        User user;
        String userName = getUserName(message);
        if (!userRepository.existsByUserId(message.getFromId())) {
             user = User.builder()
                    .userName(userName)
                    .userId(message.getFromId())
                    .chatId(message.getPeerId())
                    .build();
            userRepository.save(user);
            var keyboard = keyboardFactory.createReplyKeyboard(List.of(ADD.getText()));
            userState.getContext().add(user);
            return VkResponse.builder()
                    .message(RESPONSE_TEXT)
                    .keyboard(keyboard)
                    .build();
        }
        user = userRepository.findUserByUserId(userState.getUserId());
        userState.getContext().add(user);

        var medicalCardsString = new ArrayList<>(medicalCardRepository.findByOwnerUserId(userState.getUserId())
                .stream()
                .map(mc -> mc.getFirstName() + " " + mc.getLastName())
                .toList());
        medicalCardsString.add(ADD.getText());
        var keyboard = keyboardFactory.createReplyKeyboard(medicalCardsString);
        return VkResponse.builder()
                .keyboard(keyboard)
                .message(RESPONSE_TEXT)
                .build();
    }

    private String getUserName(Message message) {
        try {
            return vkUsersService.getUserName(message.getFromId());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void performNextState(UserState userState) {
        userState.setHandler(afterStartHandler);
    }
}
