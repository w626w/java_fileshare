package com.fileshare.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;
    private Long userId;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime createTime;
} 