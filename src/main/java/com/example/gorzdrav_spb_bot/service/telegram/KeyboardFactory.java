package com.example.gorzdrav_spb_bot.service.telegram;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyboardFactory {

    public ReplyKeyboardMarkup createReplyKeyboard(List<String> answers) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true); // Делает клавиатуру видимой только для текущего чата

        List<KeyboardRow> row = new ArrayList<>();
        for (String answer : answers) {
            KeyboardRow keyboardRow = new KeyboardRow();
            KeyboardButton button = new KeyboardButton();
            button.setText(answer);
            keyboardRow.add(button);
            row.add(keyboardRow);
        }
        markup.setKeyboard(row);

        return markup;
    }

    public ReplyKeyboardRemove getReplyKeyboardRemove() {
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true); // Удаляет клавиатуру
        keyboardRemove.setSelective(true);
        return keyboardRemove;
    }
}
