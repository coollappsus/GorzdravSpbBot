package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import java.util.List;

public record SpecialtiesResponse(
        List<Specialty> result,
        Boolean success,
        Integer errorCode,
        String message,
        String stackTrace) {
}
