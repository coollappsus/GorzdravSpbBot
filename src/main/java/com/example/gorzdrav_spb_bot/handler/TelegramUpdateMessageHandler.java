package com.example.gorzdrav_spb_bot.handler;

import com.example.gorzdrav_spb_bot.handler.dao.UserState;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramUpdateMessageHandler {

    BotApiMethod<?> processMessage(Message message, UserState userState);
}