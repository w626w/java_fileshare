package com.fileshare.controller;

import com.fileshare.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            log.debug("用户已登录: {}", userDetails.getUsername());
            model.addAttribute("currentUserId", userDetails.getId());
            model.addAttribute("username", userDetails.getUsername());
            return "index";
        } else {
            log.debug("用户未登录，重定向到登录页面");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/error")
    public String handleError(Model model) {
        if (!model.containsAttribute("error")) {
            model.addAttribute("error", "未知错误");
        }
        return "error";
    }
} 