package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import lombok.Builder;

@Builder
public record PatientRequest(
        Integer lpuId,
        String lastName,
        String firstName,
        String middleName,
        String birthdate) {
}
