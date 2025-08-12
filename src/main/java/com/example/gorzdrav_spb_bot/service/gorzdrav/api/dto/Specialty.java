package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import java.util.Date;

public record Specialty(
        Integer id,
        Integer ferId,
        String name,
        Integer countFreeParticipant,
        Integer countFreeTicket,
        Date lastDate,
        Date nearestDate
        ) {
}
