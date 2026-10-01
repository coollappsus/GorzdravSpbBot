package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public record Appointment(
        String id,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Moscow", locale = "ru")
        Date visitStart,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Moscow", locale = "ru")
        Date visitEnd,
        String address,
        String number,
        String room) {
}
