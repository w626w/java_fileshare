package com.fileshare.service.impl;

import com.fileshare.entity.OperationLog;
import com.fileshare.mapper.LogMapper;
import com.fileshare.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    private final LogMapper logMapper;
    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);

    public LogServiceImpl(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Override
    public void saveLog(OperationLog operationLog) {
        log.debug("Saving operation log: {}", operationLog);
        logMapper.insert(operationLog);
    }

    @Override
    public List<OperationLog> getAllLogs() {
        log.debug("Getting all logs");
        return logMapper.findAll();
    }

    @Override
    public List<OperationLog> searchLogs(String keyword) {
        log.debug("Searching logs with keyword: {}", keyword);
        return logMapper.searchLogs(keyword);
    }

    @Override
    public List<OperationLog> getLogs(Long userId) {
        return logMapper.findAll();
    }

    @Override
    public void exportLogs(String filePath) {
        List<OperationLog> logs = getAllLogs();
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("ID,用户ID,用户名,操作,详情,IP地址,操作时间\n");
            for (OperationLog log : logs) {
                writer.write(String.format("%d,%d,%s,%s,%s,%s,%s\n",
                    log.getId(),
                    log.getUserId(),
                    log.getUsername(),
                    log.getOperation(),
                    log.getDetails(),
                    log.getIpAddress(),
                    log.getOperationTime()
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("导出日志失败", e);
        }
    }
} 