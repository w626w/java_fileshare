package com.fileshare.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Boolean isAdmin = false;
    private Boolean isEnabled = true;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 