package com.example.gorzdrav_spb_bot.controller;

import com.example.gorzdrav_spb_bot.service.gorzdrav.sync.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class GorzdravController {

    @Autowired
    private SyncService syncService;

    @PostMapping("/run-daily-sync")
    public ResponseEntity<String> runDailySync() {
        syncService.dailySync();
        return ResponseEntity.ok("Задача синхронизации успешно запущена вручную!");
    }
}
