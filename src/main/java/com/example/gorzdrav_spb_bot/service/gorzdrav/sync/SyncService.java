package com.example.gorzdrav_spb_bot.service.gorzdrav.sync;

import com.example.gorzdrav_spb_bot.service.gorzdrav.GorzdravService;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Doctor;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.LPU;
import com.example.gorzdrav_spb_bot.service.gorzdrav.api.dto.Specialty;
import com.example.gorzdrav_spb_bot.service.telegram.TelegramAsyncMessageSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class SyncService {

    private final UpsertTransactionalSyncService upsertSyncService;
    private final GorzdravService gorzdravService;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;

    private final static int BATCH_SIZE = 500;
    private final static Long ADMIN_ID = 906044021L;

    @Scheduled(cron = "0 0 23 * * *", zone = "Europe/Moscow")
    public void dailySync() {
        try {
            LocalDate today = LocalDate.now();
            log.info("=== SYNC START {} ===", today);
            syncLPUs(today);
            syncDoctors(today);
            upsertSyncService.markStale("lpus_dict", today);
            upsertSyncService.markStale("doctors_dict", today);
            log.info("=== SYNC END {} ===", today);
        } catch (Exception e) {
            telegramAsyncMessageSender.sendMessageToUser(ADMIN_ID,
                    "Произошла ошибка при синхронизации БД Горздрава\n" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            telegramAsyncMessageSender.sendMessageToUser(ADMIN_ID, "Синхронизация с БД Горздрав завершена");
        }
    }

    private void syncLPUs(LocalDate today) {
        List<LPU> list = getLpus();
        List<Map<String, Object>> batch = new ArrayList<>();
        for (LPU dto : list) {
            batch.add(Map.of(
                    "external_id", Optional.ofNullable(dto.id()).orElse("UNKNOWN"),
                    "lpu_short_name", Optional.ofNullable(dto.lpuShortName()).orElse("UNKNOWN"),
                    "lpu_type", Optional.ofNullable(dto.lpuType()).orElse("UNKNOWN"),
                    "address", Optional.ofNullable(dto.address()).orElse("UNKNOWN"),
                    "phone", Optional.ofNullable(dto.phone()).orElse("UNKNOWN"),
                    "email", Optional.ofNullable(dto.email()).orElse("UNKNOWN"),
                    "last_seen", today
            ));
            if (batch.size() >= BATCH_SIZE) {
                upsertSyncService.upsertLPUs(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) upsertSyncService.upsertLPUs(batch);
    }

    private void syncDoctors(LocalDate today) {
        Map<Doctor, LPU> list = getDoctors();
        List<Map<String, Object>> batch = new ArrayList<>();
        for (Map.Entry<Doctor, LPU> dto : list.entrySet()) {
            batch.add(Map.of(
                    "aria_number", Optional.ofNullable(dto.getKey().ariaNumber()).orElse("UNKNOWN"),
                    "aria_type", Optional.ofNullable(dto.getKey().ariaType()).orElse("UNKNOWN"),
                    "comment", Optional.ofNullable(dto.getKey().comment()).orElse("UNKNOWN"),
                    "external_id", Optional.ofNullable(dto.getKey().id()).orElse("UNKNOWN"),
                    "name", Optional.ofNullable(dto.getKey().name()).orElse("UNKNOWN"),
                    //Тут еще бы специальность пихать, но пока оставим эту идею
                    "last_seen", today,
                    "lpu_external_id", dto.getValue().id()
            ));
            if (batch.size() >= BATCH_SIZE) {
                upsertSyncService.upsertDoctors(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            upsertSyncService.upsertDoctors(batch);
        }
    }

    private Map<Doctor, LPU> getDoctors() {
        Map<Doctor, LPU> doctors = new HashMap<>();
        for (LPU lpu : getLpus()) {
            for (Specialty specialty : getSpecialties(lpu)) {
                List<Doctor> doctorList = getDoctors(lpu, specialty);
                for (Doctor doctor : doctorList) {
                    doctors.put(doctor, lpu);
                }
            }
        }
        return doctors;
    }

    private List<Doctor> getDoctors(LPU lpu, Specialty specialty) {
        try {
            return gorzdravService.getDoctors(specialty, lpu);
        } catch (Exception e) {
            log.error("Произошла ошибка при сборе докторов по специальности {} в ЛПУ {}\n{}",
                    specialty.name(), lpu.lpuShortName(), e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Specialty> getSpecialties(LPU lpu) {
        try {
            return gorzdravService.getSpecialties(lpu);
        } catch (Exception e) {
            log.error("Произошла ошибка при сборе специалистов в ЛПУ {}\n{}", lpu.lpuShortName(), e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<LPU> getLpus() {
        return gorzdravService.getAllLPUs();
    }
}