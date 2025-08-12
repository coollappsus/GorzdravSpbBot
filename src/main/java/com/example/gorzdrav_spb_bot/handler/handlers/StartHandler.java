package com.example.gorzdrav_spb_bot.handler.handlers;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.model.User;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import com.example.gorzdrav_spb_bot.repository.UserRepository;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.ADD;

@Component
@AllArgsConstructor
public class StartHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT = "Выберите мед.карту или добавьте новую";

    private final MedicalCardRepository medicalCardRepository;
    private final UserRepository userRepository;
    private final KeyboardFactory keyboardFactory;
    private final TelegramUpdateMessageHandler afterStartHandler;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        performNextState(userState);
        User user;
        if (!userRepository.existsByUserId(message.getFrom().getId())) {
             user = User.builder()
                    .userName(message.getFrom().getFirstName())
                    .userId(message.getFrom().getId())
                    .chatId(message.getChatId())
                    .build();
            userRepository.save(user);
            var keyboard = keyboardFactory.createReplyKeyboard(List.of(ADD.getText()));
            userState.getContext().add(user);
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .replyMarkup(keyboard)
                    .text(RESPONSE_TEXT)
                    .build();
        }
        user = userRepository.findUserByUserId(userState.getUserId());
        userState.getContext().add(user);

        var medicalCardsString = new ArrayList<>(medicalCardRepository.findByOwnerUserId(userState.getUserId()).stream()
                .map(mc -> mc.getFirstName() + " " + mc.getLastName())
                .toList());
        medicalCardsString.add(ADD.getText());
        var keyboard = keyboardFactory.createReplyKeyboard(medicalCardsString);
        return SendMessage.builder()
                    .chatId(message.getChatId())
                    .replyMarkup(keyboard)
                    .text(RESPONSE_TEXT)
                    .build();
    }

    private void performNextState(UserState userState) {
        userState.setHandler(afterStartHandler);
    }
}
