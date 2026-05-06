package com.example.gorzdrav_spb_bot.handler.dao;

import java.time.LocalDate;

public record SelectedAppointmentDay(
        LocalDate date,
        String formattedDay) {
}
