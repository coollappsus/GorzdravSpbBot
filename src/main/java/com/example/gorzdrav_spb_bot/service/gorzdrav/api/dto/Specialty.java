package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import lombok.Builder;

import java.util.Date;

@Builder
public record Specialty(
        String id,
        Integer ferId,
        String name,
        Integer countFreeParticipant,
        Integer countFreeTicket,
        Date lastDate,
        Date nearestDate
        ) {
}
