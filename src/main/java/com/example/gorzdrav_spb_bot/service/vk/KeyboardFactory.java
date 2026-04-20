package com.example.gorzdrav_spb_bot.service.vk;

import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.KeyboardButton;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyboardFactory {

    // Лимит ВК: максимум 5 кнопок в одном ряду
    private static final int MAX_COLUMNS = 5;
    // Лимит ВК: максимум 10 рядов
    private static final int MAX_ROWS = 10;

    public Keyboard createReplyKeyboard(List<String> allCommands) {
        List<List<KeyboardButton>> buttons = new ArrayList<>();
        List<KeyboardButton> currentRow = new ArrayList<>();

        for (String cmd : allCommands) {
            // Если мы уже создали 10 рядов, прекращаем, чтобы не получить ошибку от API
            if (buttons.size() >= MAX_ROWS) {
                break;
            }

            // Если текущий ряд заполнен, добавляем его в общую сетку и создаем новый
            if (currentRow.size() == MAX_COLUMNS) {
                buttons.add(currentRow);
                currentRow = new ArrayList<>();
            }

            // Создаем кнопку и добавляем в текущий ряд
            currentRow.add(new KeyboardButton()
                    .setAction(new KeyboardButtonActionText()
                            .setType(KeyboardButtonActionTextType.TEXT)
                            .setLabel(truncateLabel(cmd)))
                    .setColor(KeyboardButtonColor.PRIMARY));
        }

        // Не забываем добавить последний ряд, если он не пустой
        if (!currentRow.isEmpty() && buttons.size() < MAX_ROWS) {
            buttons.add(currentRow);
        }

        return new Keyboard()
                .setButtons(buttons)
                .setOneTime(false)
                .setInline(false); // Для обычных кнопок под полем ввода
    }

    public String truncateLabel(String text) {
        if (text == null) return "";
        return text.length() <= 40 ? text : text.substring(0, 37) + "...";
    }

}
