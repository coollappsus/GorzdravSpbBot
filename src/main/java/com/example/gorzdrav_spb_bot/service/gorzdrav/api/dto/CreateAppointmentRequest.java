package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import lombok.Builder;

@Builder
public record CreateAppointmentRequest(
        String lpuId,
        String patientId,
        String appointmentId) {
}
