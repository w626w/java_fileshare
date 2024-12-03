package com.fileshare.config;

import com.fileshare.entity.User;
import com.fileshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialDataConfig implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) {
        // 检查是否已存在管理员用户
        if (userService.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // 将在 service 层加密
            admin.setEmail("admin@example.com");
            admin.setIsAdmin(true);
            userService.createUser(admin);
        }
    }
} 