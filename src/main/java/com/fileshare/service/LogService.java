package com.fileshare.service;

import com.fileshare.entity.OperationLog;
import java.util.List;

public interface LogService {
    void saveLog(OperationLog log);
    List<OperationLog> getLogs(Long userId);
    List<OperationLog> getAllLogs();
    List<OperationLog> searchLogs(String keyword);
    void exportLogs(String filePath);
} 