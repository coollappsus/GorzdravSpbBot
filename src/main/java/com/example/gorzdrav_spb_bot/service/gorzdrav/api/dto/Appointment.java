package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import java.util.Date;

public record Appointment(
        String id,
        Date visitStart,
        Date visitEnd,
        String address,
        String number,
        String room) {
}
