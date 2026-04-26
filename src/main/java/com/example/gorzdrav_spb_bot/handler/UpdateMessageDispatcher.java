package com.example.gorzdrav_spb_bot.handler;

import api.longpoll.bots.model.objects.basic.Message;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.handler.util.ContextUtil;
import com.example.gorzdrav_spb_bot.service.vk.VkAsyncMessageSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UpdateMessageDispatcher {

    private static final String CLEAR_CONTEXT_RESPONSE = "Весь контекст очищен, начнем сначала";

    private final ContextUtil contextUtil;
    private final StartHandler startHandler;
    private final VkAsyncMessageSender vkAsyncMessageSender;

    public VkResponse processMessage(Message message, UserState userState) {
        log.info("Начало обработки сообщения: message={}", message);

        String messageText = message.getText();
        if (isCommand(messageText)) {
            //TODO: написать нормальные обработчики команд.
            // Но вроде как распыляться ради одной команды ту мач пока что
            if (messageText.equals("/clear")) {
                contextUtil.cleanAllContext(userState);
                userState.setHandler(startHandler);
                vkAsyncMessageSender.sendMessageToUser(Long.valueOf(message.getPeerId()), CLEAR_CONTEXT_RESPONSE);
            }
        }

        return userState.getHandler().processMessage(message, userState);
    }

    private boolean isCommand(String command) {
        return command.startsWith("/") && command.length() > 1;
    }
}
