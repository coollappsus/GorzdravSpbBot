package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

public record PatientResponse(
        String result,
        Boolean success,
        Integer errorCode,
        String message,
        String stackTrace) {
}
