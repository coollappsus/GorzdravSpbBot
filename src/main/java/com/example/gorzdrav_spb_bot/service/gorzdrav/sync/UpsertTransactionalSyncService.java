package com.example.gorzdrav_spb_bot.service.gorzdrav.sync;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class UpsertTransactionalSyncService {

    private final NamedParameterJdbcTemplate db;

    @Transactional
    protected void upsertLPUs(List<Map<String, Object>> batch) {
        String sql = """
                INSERT INTO public.lpus_dict(external_id, lpu_short_name, lpu_type, address, phone, email, last_seen, updated_at)
                VALUES(:external_id, :lpu_short_name, :lpu_type, :address, :phone, :email, :last_seen, now())
                ON CONFLICT (external_id) DO UPDATE
                  SET lpu_short_name    = EXCLUDED.lpu_short_name,
                      lpu_type          = EXCLUDED.lpu_type,
                      address           = EXCLUDED.address,
                      phone             = EXCLUDED.phone,
                      email             = EXCLUDED.email,
                      last_seen         = EXCLUDED.last_seen,
                      updated_at        = now(),
                      deleted           = false
                """;
        int[] res = db.batchUpdate(sql,
                SqlParameterSourceUtils.createBatch(batch.toArray()));
        log.info("Upserted {} LPUs", Arrays.stream(res).sum());
    }

    @Transactional
    protected void upsertDoctors(List<Map<String, Object>> batch) {
        String up = """
                INSERT INTO public.doctors_dict(aria_number, aria_type, comment, external_id, name, specialty, last_seen, updated_at, lpu_external_id)
                VALUES(:aria_number, :aria_type, :comment, :external_id, :name, :specialty, :last_seen, now(), :lpu_external_id)
                ON CONFLICT (external_id, "name", aria_number, aria_type, lpu_external_id) DO UPDATE
                  SET aria_number       = EXCLUDED.aria_number,
                      aria_type         = EXCLUDED.aria_type,
                      "comment"         = EXCLUDED.comment,
                      external_id       = EXCLUDED.external_id,
                      "name"            = EXCLUDED.name,
                      specialty         = EXCLUDED.specialty,
                      last_seen         = EXCLUDED.last_seen,
                      updated_at        = now(),
                      deleted           = false,
                      lpu_external_id   = EXCLUDED.lpu_external_id
                """;
        int[] res = db.batchUpdate(up,
                SqlParameterSourceUtils.createBatch(batch.toArray()));
        log.info("Upserted {} doctors", Arrays.stream(res).sum());
    }

    @Transactional
    protected void markStale(String table, LocalDate today) {
        String sql = String.format("""
                UPDATE %s
                SET deleted = true, updated_at = now()
                WHERE last_seen < :today AND deleted = false
                """, table);
        int cnt = db.update(sql, Map.of("today", today));
        log.info("Marked {} stale rows in {}", cnt, table);
    }
}
