package com.example.gorzdrav_spb_bot.handler.handlers.medicalCard.remove;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.cache.util.ContextUtil;
import com.example.gorzdrav_spb_bot.handler.handlers.StartHandler;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class MedicalCardRemoveHandler implements TelegramUpdateMessageHandler {

    private static final String RESPONSE_TEXT_REMOVE = "Мед.карта удалена";

    private final MedicalCardRepository medicalCardRepository;
    private final ContextUtil contextUtil;
    private final StartHandler startHandler;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    @Override
    public BotApiMethod<?> processMessage(Message message, UserState userState) {
        medicalCardRepository.deleteById(userState.getMedicalCardContext().getId());
        contextUtil.cleanAllContext(userState);
        telegramAsyncMessageSender.sendMessageToUser(message.getChatId(), RESPONSE_TEXT_REMOVE);
        return startHandler.processMessage(message, userState);
    }
}
