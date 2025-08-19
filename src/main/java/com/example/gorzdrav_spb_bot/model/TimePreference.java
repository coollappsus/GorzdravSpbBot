package com.example.gorzdrav_spb_bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TimePreference {
    EARLY("Утро"),   // с 08:00 по 12:00
    MID("День"),     // 12:00–17:00
    LATE("Вечер");     // 17:00–20:00

    private final String additionalName;
}
