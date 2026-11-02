package com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto;

import lombok.Builder;

@Builder
public record LPU(
        String id,
        Boolean isActive,
        String lpuShortName,
        String lpuType,
        String address,
        String phone,
        String email
        ) {
}
