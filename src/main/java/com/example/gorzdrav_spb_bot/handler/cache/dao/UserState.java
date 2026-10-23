package com.example.gorzdrav_spb_bot.handler.cache.dao;

import com.example.gorzdrav_spb_bot.model.TimePreference;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserState implements Serializable {
        private Long userId;
        private String handlerName;
        UserContext userContext;
        MedicalCardContext medicalCardContext;
        District district;
        LPU lpu;
        Doctor doctor;
        Specialty specialty;
        Appointment appointment;
        Date preferedDate;
        TimePreference timePreference;
}
