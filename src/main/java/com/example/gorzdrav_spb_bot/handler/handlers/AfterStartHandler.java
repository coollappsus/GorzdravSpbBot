package com.example.gorzdrav_spb_bot.handler.handlers;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.UserConstResponseText;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.add.AddMedCardFirstNameHandler;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.model.User;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import com.example.gorzdrav_spb_bot.service.vk.KeyboardFactory;
import com.vk.api.sdk.objects.messages.Message;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.*;

@Component
@AllArgsConstructor
public class AfterStartHandler implements VkUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FIRST_NAME = "Введите имя пациента";
    private static final String RESPONSE_TEXT_CHOSE_ACTION = "Выберите действие с мед.картой";

    private final KeyboardFactory keyboardFactory;
    private final AddMedCardFirstNameHandler addMedCardFirstNameHandler;
    private final MedicalCardRepository medicalCardRepository;
    private final ChooseActionHandler chooseActionHandler;
    private final ContextUtil contextUtil;

    @Override
    public VkResponse processMessage(Message message, UserState userState) {
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
            return VkResponse.builder()
                    .keyboard(null)
                    .message(RESPONSE_TEXT_FIRST_NAME)
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

        return VkResponse.builder()
                .keyboard(keyboard)
                .message(RESPONSE_TEXT_CHOSE_ACTION)
                .build();
    }

    private String[] getSplitText(String text) {
        return text.split(" ");
    }
}
