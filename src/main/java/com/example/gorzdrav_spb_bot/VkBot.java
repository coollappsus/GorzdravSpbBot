package com.example.gorzdrav_spb_bot;

import api.longpoll.bots.LongPollBot;
import api.longpoll.bots.model.events.messages.MessageNew;
import api.longpoll.bots.model.objects.basic.Message;
import com.example.gorzdrav_spb_bot.handler.MessageHandler;
import com.example.gorzdrav_spb_bot.service.vk.VkAsyncMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.example.gorzdrav_spb_bot.config.Const.ADMIN_ID;

@Slf4j
@Component
public class VkBot extends LongPollBot {

    private final MessageHandler messageHandler;
    private final VkAsyncMessageSender vkAsyncMessageSender;

    @Autowired
    public VkBot(MessageHandler messageHandler, VkAsyncMessageSender vkAsyncMessageSender) {
        this.messageHandler = messageHandler;
        this.vkAsyncMessageSender = vkAsyncMessageSender;
    }

    @Override
    public void onMessageNew(MessageNew messageNew) {
        try {
            Message message = messageNew.getMessage();
            messageHandler.handle(message);
        } catch (Exception e) {
            sendMessageError(e);
        }
    }

    public void sendMessageError(Exception e) {
        vkAsyncMessageSender.sendMessageToUser(ADMIN_ID, e.getMessage());
    }

    @Override
    public String getAccessToken() {
        return System.getProperty("vk.access-token");
    }
}
