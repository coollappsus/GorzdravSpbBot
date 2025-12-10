package com.example.gorzdrav_spb_bot.service.gorzdrav.sync.dto;

import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Specialty;
import lombok.Builder;

@Builder
public record DoctorInfo(
        Doctor doctor,
        LPU lpu,
        Specialty specialty) {
}
