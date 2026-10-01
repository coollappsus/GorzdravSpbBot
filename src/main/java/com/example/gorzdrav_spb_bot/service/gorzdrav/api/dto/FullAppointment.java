package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public record FullAppointment(
        String appointmentId,
        String lpuId,
        String patientId,
        String lpuShortName,
        Doctor doctorRendingConsultation,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Moscow", locale = "ru")
        Date visitStart) {
}
