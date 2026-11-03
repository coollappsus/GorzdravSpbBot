package com.example.gorzdrav_spb_bot.handler.cache.dao;

import com.example.gorzdrav_spb_bot.model.TimePreference;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
        List<TaskContext> tasks;
        List<String> appointmentIds;
}
