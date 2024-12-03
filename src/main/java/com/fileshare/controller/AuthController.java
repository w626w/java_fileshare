package com.fileshare.controller;

import com.fileshare.entity.User;
import com.fileshare.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    @ResponseBody
    public String registerUser(@RequestBody User user) {
        log.debug("Registering user: {}", user.getUsername());
        if (userService.findByUsername(user.getUsername()) != null) {
            return "用户名已存在";
        }
        if ("admin".equals(user.getUsername().toLowerCase())) {
            log.debug("Setting admin role for user: {}", user.getUsername());
            user.setRole("ROLE_ADMIN");
            user.setIsAdmin(true);
        } else {
            user.setRole("ROLE_USER");
            user.setIsAdmin(false);
        }
        user.setIsEnabled(true);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        log.debug("User to be created: {}", user);
        userService.createUser(user);
        return "注册成功";
    }
} 