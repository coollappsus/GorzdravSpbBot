package com.example.gorzdrav_spb_bot.handler;

import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Service
@AllArgsConstructor
public class TelegramUpdateMessageDispatcher {

    private static final String CLEAR_CONTEXT_RESPONSE = "Весь контекст очищен, начнем сначала";

    private final ContextUtil contextUtil;
    private final StartHandler startHandler;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        log.info("Начало обработки сообщения: message={}", message);

        if (message.isCommand()) {
            //TODO: написать нормальные обработчики команд.
            // Но вроде как распыляться ради одной команды ту мач пока что
            if (message.getText().equals("/clear")) {
                contextUtil.cleanAllContext(userState);
                userState.setHandler(startHandler);
                telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), CLEAR_CONTEXT_RESPONSE);
            }
        }

        return userState.getHandler().processMessage(message, userState);
    }
}
