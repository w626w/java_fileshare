package com.fileshare.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperationLog {
    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String details;
    private String ipAddress;
    private LocalDateTime operationTime;
} 