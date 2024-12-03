package com.fileshare.controller;

import com.fileshare.entity.OperationLog;
import com.fileshare.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@PreAuthorize("hasRole('ADMIN')")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping
    public ResponseEntity<List<OperationLog>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }

    @GetMapping("/search")
    public ResponseEntity<List<OperationLog>> searchLogs(@RequestParam String keyword) {
        return ResponseEntity.ok(logService.searchLogs(keyword));
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportLogs() {
        String filePath = "logs_" + System.currentTimeMillis() + ".csv";
        logService.exportLogs(filePath);
        return ResponseEntity.ok("日志已导出到: " + filePath);
    }
} 