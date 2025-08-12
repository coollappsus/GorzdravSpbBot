package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import java.util.Date;

public record FullAppointment(
        String appointmentId,
        String lpuId,
        String patientId,
        String lpuShortName,
        Doctor doctorRendingConsultation,
        Date visitStart) {
}
