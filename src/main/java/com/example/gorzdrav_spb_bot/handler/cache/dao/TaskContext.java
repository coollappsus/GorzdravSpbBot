package com.example.gorzdrav_spb_bot.handler.cache.dao;

import com.example.gorzdrav_spb_bot.model.TimePreference;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Builder
@Getter
public class TaskContext {

    private Long id;
    private UserContext owner;
    private MedicalCardContext medicalCard;
    private String lpuId;
    private String doctorId;

    private boolean completeStatus;
    private boolean activeStatus;

    private TimePreference timePreference;
    private Date preferenceDate;
}
