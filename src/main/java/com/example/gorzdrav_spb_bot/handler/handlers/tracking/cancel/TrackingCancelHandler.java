package com.example.gorzdrav_spb_bot.handler.handlers.tracking.cancel;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.cache.util.ContextUtil;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.model.Task;
import com.example.gorzdrav_spb_bot.repository.TaskRepository;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@AllArgsConstructor
public class TrackingCancelHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_FINISH_CANCEL = "Поздравляю! Отслеживание отменено✨";

    private final TaskRepository taskRepository;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;
    private final ContextUtil contextUtil;
    private final StartHandler startHandler;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        String inputText = message.getText();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(inputText);
        if (matcher.find()) {
            Long taskId = Long.valueOf(matcher.group());
            Task task = taskRepository.findById(taskId).orElseThrow();
            task.doCancel();
            taskRepository.save(task);
        } else {
            telegramAsyncMessageSender.sendMessageToUser(message.getChatId(),
                    "Пожалуйста, выберите номер из списка на клавиатуре.");
            return null;
        }
        telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), RESPONSE_TEXT_FINISH_CANCEL);
        contextUtil.cleanAllContext(userState);

        return startHandler.processMessage(message, userState);
    }
}
