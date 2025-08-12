package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import java.util.List;

public record AppointmentsResponse(
        List<Appointment> result,
        Boolean success,
        Integer errorCode,
        String message,
        String stackTrace) {
}
