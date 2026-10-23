package com.example.gorzdrav_spb_bot.handler.cache.dao;

import lombok.Builder;

@Builder
public record UserContext(
        Long id,
        String userName,
        Long userId,
        Long chatId
) {
}
