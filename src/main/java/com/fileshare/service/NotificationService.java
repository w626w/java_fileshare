package com.fileshare.service;

import com.fileshare.entity.Notification;
import java.util.List;

public interface NotificationService {
    void sendNotification(Long userId, String message, String type);
    List<Notification> getUserNotifications(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void deleteNotification(Long notificationId);
    List<Notification> getNotifications(Long userId);
    int getUnreadCount(Long userId);
} 