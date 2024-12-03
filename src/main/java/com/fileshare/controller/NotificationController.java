package com.fileshare.controller;

import com.fileshare.entity.Notification;
import com.fileshare.security.CustomUserDetails;
import com.fileshare.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Notification> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.getNotifications(userDetails.getId());
    }

    @GetMapping("/unread/count")
    public int getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.getUnreadCount(userDetails.getId());
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAsRead(id, userDetails.getId());
    }
} 