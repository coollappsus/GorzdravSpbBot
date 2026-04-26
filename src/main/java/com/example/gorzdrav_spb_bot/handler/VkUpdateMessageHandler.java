package com.example.gorzdrav_spb_bot.handler;

import api.longpoll.bots.model.objects.basic.Message;
import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;

public interface VkUpdateMessageHandler {

    VkResponse processMessage(Message message, UserState userState);
}