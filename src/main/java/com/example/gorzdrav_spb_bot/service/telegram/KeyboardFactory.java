package com.example.gorzdrav_spb_bot.service.telegram;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.gorzdrav_spb_bot.handler.UserConstResponseText.TO_MAIN;

@Service
public class KeyboardFactory {

    public ReplyKeyboardMarkup createReplyKeyboard(List<String> answers) {
        return createReplyKeyboard(answers, true);
    }

    public ReplyKeyboardMarkup createReplyKeyboard(List<String> answers, boolean shouldIncludeToMain) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);

        List<KeyboardRow> rows = answers.stream()
                .map(answer -> {
                    KeyboardRow row = new KeyboardRow();
                    row.add(new KeyboardButton(answer));
                    return row;
                })
                .collect(Collectors.toList());

        if (shouldIncludeToMain) {
            KeyboardRow mainRow = new KeyboardRow();
            mainRow.add(new KeyboardButton("🏠 " + TO_MAIN.getText()));
            rows.add(mainRow);
        }
        markup.setKeyboard(rows);
        return markup;
    }

    public ReplyKeyboardRemove getReplyKeyboardRemove() {
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true); // Удаляет клавиатуру
        keyboardRemove.setSelective(true);
        return keyboardRemove;
    }
}
