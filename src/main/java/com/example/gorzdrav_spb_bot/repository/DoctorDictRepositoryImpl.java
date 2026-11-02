package com.example.gorzdrav_spb_bot.repository;

import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
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
        return getDoctorInfoByDoctorExternalIdAndLpuExternalId(doctorExternalId, lpuExternalId, false);
    }

    @Nullable
    public DoctorInfo getDoctorInfoByDoctorExternalIdAndLpuExternalId(String doctorExternalId, String lpuExternalId,
                                                                      boolean shouldJoinLPU) {
        log.info("Получение информации о враче (with join: {})", shouldJoinLPU);

        String selectFields = shouldJoinLPU
                ? "dd.name, dd.specialty, ld.lpu_short_name, ld.address "
                : "dd.name, dd.specialty ";
        StringBuilder sql = new StringBuilder("select ")
                .append(selectFields)
                .append("from public.doctors_dict dd ");
        if (shouldJoinLPU) {
            sql.append("join public.lpus_dict ld on dd.lpu_external_id = ld.external_id ");
        }
        sql.append("where dd.external_id = :doctorExternalId and dd.lpu_external_id = :lpuExternalId");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("doctorExternalId", doctorExternalId);
        query.setParameter("lpuExternalId", lpuExternalId);

        return executeQuery(query, shouldJoinLPU);
    }

    private DoctorInfo executeQuery(Query query, boolean shouldJoinLPU) {
        try {
            Object[] row = (Object[]) query.getSingleResult();
            String name = (String) row[0];
            String specialty = (String) row[1];
            String lpuShortName = "";
            String address = "";
            if (shouldJoinLPU) {
                lpuShortName = (String) row[2];
                address = (String) row[3];
            }
            return createDoctorInfo(name, specialty, lpuShortName, address);
        } catch (NoResultException ex) {
            log.error("Не найдено информации по врачу\n" + ex.getMessage());
            return null;
        } catch (NonUniqueResultException ex) {
            log.error("По этим данным найдено более одного врача\n" + ex.getMessage());
            return null;
        }
    }

    private DoctorInfo createDoctorInfo(String name, String specialty, String lpuShortName, String address) {
        return DoctorInfo.builder()
                .doctor(Doctor.builder()
                        .name(name)
                        .build())
                .specialty(Specialty.builder()
                        .name(specialty)
                        .build())
                .lpu(LPU.builder()
                        .lpuShortName(lpuShortName)
                        .address(address)
                        .build())
                .build();
    }
}
