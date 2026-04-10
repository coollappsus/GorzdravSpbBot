package com.example.gorzdrav_spb_bot.handler.dao;

import com.example.gorzdrav_spb_bot.handler.VkUpdateMessageHandler;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder
@Data
public class UserState {
        Long userId;
        VkUpdateMessageHandler handler;
        Set<Object> context;
}
