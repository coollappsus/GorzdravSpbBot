package com.example.gorzdrav_spb_bot.handler.cache.dao;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalCardContext implements Serializable {
        Long id;
        UserContext owner;
        String firstName;
        String lastName;
        String middleName;
        Date birthDate;
        int lpuId;
        String patientId;
}
