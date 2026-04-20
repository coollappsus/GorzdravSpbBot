package com.example.gorzdrav_spb_bot.service.vk;

import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.KeyboardButton;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class KeyboardFactory {

    public Keyboard createReplyKeyboard(List<String> allCommands) {
        List<List<KeyboardButton>> buttons = new ArrayList<>();

        // Генерируем кнопки динамически на основе списка
        for (String cmd : allCommands) {
            buttons.add(Collections.singletonList(
                    new KeyboardButton()
                            .setAction(new KeyboardButtonActionText()
                                    .setType(KeyboardButtonActionTextType.TEXT)
                                    .setLabel(cmd)
                                    .setPayload(cmd))
                            .setColor(KeyboardButtonColor.PRIMARY)
            ));
        }

        return new Keyboard().setButtons(buttons).setOneTime(false);
    }
}
