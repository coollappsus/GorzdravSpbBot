package com.example.gorzdrav_spb_bot.service.vk;

import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.KeyboardButton;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class KeyboardFactory {

    public Keyboard createReplyKeyboard(Collection<String> allCommands) {
        List<List<KeyboardButton>> buttons = new ArrayList<>();
        List<KeyboardButton> currentRow = new ArrayList<>();

        // Лимит ВК: максимум 10 рядов
        int maxRows = 8;
        // Лимит ВК: максимум 5 кнопок в одном ряду, но если кнопок не много, засунем по одной в ряд
        int maxColumns = 5;
        if (allCommands.size() <= maxColumns) {
            maxColumns = 1;
        }

        for (String cmd : allCommands) {
            // Если мы уже создали 10 рядов, прекращаем, чтобы не получить ошибку от API
            if (buttons.size() >= maxRows) {
                break;
            }

            // Если текущий ряд заполнен, добавляем его в общую сетку и создаем новый
            if (currentRow.size() == maxColumns) {
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
        if (!currentRow.isEmpty() && buttons.size() < maxRows) {
            buttons.add(currentRow);
        }

        return new Keyboard()
                .setButtons(buttons)
                .setOneTime(false)
                .setInline(false); // Для обычных кнопок под полем ввода
    }

    private String truncateLabel(String text) {
        if (text == null) return "";
        return text.length() <= 40 ? text : text.substring(0, 37) + "...";
    }

}
