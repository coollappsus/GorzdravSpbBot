package com.example.gorzdrav_spb_bot.repository;

import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Specialty;
import com.example.gorzdrav_spb_bot.service.gorzdrav.sync.dto.DoctorInfo;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class DoctorDictRepositoryImpl implements org.springframework.data.repository.Repository<DoctorInfo, Long> {

    @PersistenceContext
    EntityManager entityManager;

    @Nullable
    public DoctorInfo getDoctorInfoByDoctorExternalIdAndLpuExternalId(String doctorExternalId, String lpuExternalId) {
        log.info("Получение информации о враче из собственных справочников");
        String select = "select dd.name, dd.specialty " +
                "from public.doctors_dict dd " +
                "where dd.external_id = :doctorExternalId " +
                "and dd.lpu_external_id = :lpuExternalId";
        Query query = entityManager.createNativeQuery(select);

        query.setParameter("doctorExternalId", doctorExternalId);
        query.setParameter("lpuExternalId", lpuExternalId);

        return executeQuery(query);
    }

    private DoctorInfo executeQuery(Query query) {
        try {
            Object[] row = (Object[]) query.getSingleResult();
            String name = (String) row[0];
            String specialty = (String) row[1];
            return createDoctorInfo(name, specialty);
        } catch (NoResultException ex) {
            log.error("Не найдено информации по врачу\n" + ex.getMessage());
            return null;
        } catch (NonUniqueResultException ex) {
            log.error("По этим данным найдено более одного врача\n" + ex.getMessage());
            return null;
        }
    }

    private DoctorInfo createDoctorInfo(String name, String specialty) {
        return DoctorInfo.builder()
                .doctor(Doctor.builder()
                        .name(name)
                        .build())
                .specialty(Specialty.builder()
                        .name(specialty)
                        .build())
                .build();
    }
}
