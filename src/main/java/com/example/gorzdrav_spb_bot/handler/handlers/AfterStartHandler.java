package com.example.gorzdrav_spb_bot.handler.handlers;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.UserConstResponseText;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.add.AddMedCardFirstNameHandler;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.model.User;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import com.example.gorzdrav_spb_bot.service.telegram.KeyboardFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;

import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.*;

@Component
@AllArgsConstructor
public class AfterStartHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FIRST_NAME = "Введите имя пациента";
    private static final String RESPONSE_TEXT_CHOSE_ACTION = "Выберите действие с мед.картой";

    private final KeyboardFactory keyboardFactory;
    private final AddMedCardFirstNameHandler addMedCardFirstNameHandler;
    private final MedicalCardRepository medicalCardRepository;
    private final ChooseActionHandler chooseActionHandler;
    private final ContextUtil contextUtil;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        var user = contextUtil.getContextObject(userState, User.class);

        if (message.getText().equals(ADD.getText())) {
            if (medicalCardRepository.countByOwnerUserId(user.getUserId()) == 5) {
                throw new RuntimeException("Невозможно добавить мед.карту. Количество существующих карт равно 5");
            }
            var medicalCard = MedicalCard.builder()
                    .owner(user)
                    .build();
            userState.getContext().add(medicalCard);
            userState.setHandler(addMedCardFirstNameHandler);
            return SendMessage.builder()
                    .replyMarkup(keyboardFactory.getReplyKeyboardRemove())
                    .chatId(message.getChatId())
                    .text(RESPONSE_TEXT_FIRST_NAME)
                    .build();
        }

        var splitText = getSplitText(message.getText());
        var medicalCard = medicalCardRepository.findByOwnerAndFirstNameAndLastName(user, splitText[0], splitText[1]);
        userState.getContext().add(medicalCard);

        var responses = Arrays.stream(UserConstResponseText.values()).map(UserConstResponseText::getText)
                .filter(text -> !text.equals(TO_MAIN.getText()) && !text.equals(ADD.getText())
                        && !text.equals(CONFIRMATION.getText()))
                .toList();
        var keyboard = keyboardFactory.createReplyKeyboard(responses);
        userState.setHandler(chooseActionHandler);

        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboard)
                .text(RESPONSE_TEXT_CHOSE_ACTION)
                .build();
    }

    private String[] getSplitText(String text) {
        return text.split(" ");
    }
}
