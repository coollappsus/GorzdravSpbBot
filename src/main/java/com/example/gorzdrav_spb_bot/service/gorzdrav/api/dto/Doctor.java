package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import lombok.Builder;

import java.util.Date;

@Builder
public record Doctor(
        String ariaNumber,
        String ariaType,
        String comment,
        Integer freeParticipantCount,
        Integer freeTicketCount,
        String id,
        Date lastDate,
        String name,
        Date nearestDate) {
}
