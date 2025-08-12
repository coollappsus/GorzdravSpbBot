package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import lombok.Builder;

@Builder
public record CancelAppointmentRequest(
        String appointmentId,
        String lpuId,
        String patientId) {
}
