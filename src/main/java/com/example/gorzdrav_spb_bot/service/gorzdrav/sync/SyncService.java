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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                    "external_id", dto.id(),
                    "lpu_short_name", dto.lpuShortName(),
                    "lpu_type", dto.lpuType(),
                    "address", dto.address(),
                    "phone", dto.phone(),
                    "email", dto.email(),
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
        List<Doctor> list = getDoctors();
        List<Map<String, Object>> batch = new ArrayList<>();
        for (Doctor dto : list) {
            batch.add(Map.of(
                    "aria_number", dto.ariaNumber(),
                    "aria_type", dto.ariaType(),
                    "comment", dto.comment(),
                    "external_id", dto.id(),
                    "name", dto.name(),
                    //Тут еще бы специальность пихать, но пока оставим эту идею
                    "last_seen", today
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

    private List<Doctor> getDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        for (LPU lpu : getLpus()) {
            for (Specialty specialty : getSpecialties(lpu)) {
                doctors.addAll(gorzdravService.getDoctors(specialty, lpu));
            }
        }
        return doctors;
    }

    private List<Specialty> getSpecialties(LPU lpu) {
        return gorzdravService.getSpecialties(lpu);
    }

    private List<LPU> getLpus() {
        return gorzdravService.getAllLPUs();
    }
}