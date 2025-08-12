package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import java.util.List;

public record DistrictsResponse(
        List<District> result,
        Boolean success,
        Integer errorCode,
        String message,
        String stackTrace) {
}
