package com.example.gorzdrav_spb_bot.handler.cache.util;

import com.example.gorzdrav_spb_bot.handler.cache.UserStateService;
import com.example.gorzdrav_spb_bot.handler.cache.dao.MedicalCardContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserContext;
import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ContextUtil {

    private final UserStateService userStateService;

    public void cleanAllContext(UserState userState) {
        userStateService.deleteByUserId(userState.getUserId());
    }

    public void addUser(UserState userState, User user) {
        userState.setUserContext(
                UserContext.builder()
                        .id(user.getId())
                        .userName(user.getUserName())
                        .chatId(user.getChatId())
                        .userId(user.getUserId())
                        .build());
    }

    public void addMedicalCard(UserState userState, MedicalCard medicalCard) {
        userState.setMedicalCardContext(
                MedicalCardContext.builder()
                        .id(medicalCard.getId())
                        .owner(userState.getUserContext())
                        .lpuId(medicalCard.getLpuId())
                        .birthDate(medicalCard.getBirthDate())
                        .firstName(medicalCard.getFirstName())
                        .lastName(medicalCard.getLastName())
                        .middleName(medicalCard.getMiddleName())
                        .patientId(medicalCard.getPatientId())
                        .build()
        );
    }
}
