package com.example.gorzdrav_spb_bot.handler.dao;

import com.example.gorzdrav_spb_bot.handler.TelegramUpdateMessageHandler;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Builder
@Getter
@Setter
public class UserState {
        Long userId;
        TelegramUpdateMessageHandler handler;
        Set<Object> context;
}
