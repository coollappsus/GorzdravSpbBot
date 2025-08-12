package com.example.gorzdrav_spb_bot.service;

import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.model.User;
import com.example.gorzdrav_spb_bot.repository.MedicalCardRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
public class MedicalCardService {

    private final MedicalCardRepository medicalCardRepository;

    public void addMedicalCard(User user, String firstName, String lastName, String middleName, Date birthDate,
                               String patientId) {
        medicalCardRepository.save(MedicalCard.builder()
                .owner(user)
                .firstName(firstName)
                .MiddleName(middleName)
                .lastName(lastName)
                .birthDate(birthDate)
                .patientId(patientId)
                .build());
    }
}
