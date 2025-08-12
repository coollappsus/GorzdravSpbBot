package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

public record DefaultResponse(
        Boolean success,
        Integer errorCode,
        String message,
        String stackTrace) {
}
