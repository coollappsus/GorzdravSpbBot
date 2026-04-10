package com.example.gorzdrav_spb_bot.handler;

import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import com.example.gorzdrav_spb_bot.handler.dao.VkResponse;
import com.vk.api.sdk.objects.messages.Message;

public interface VkUpdateMessageHandler {

    VkResponse processMessage(Message message, UserState userState);
}