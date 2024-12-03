package com.fileshare.service.impl;

import com.fileshare.entity.Notification;
import com.fileshare.mapper.NotificationMapper;
import com.fileshare.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotification(Long userId, String message, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreateTime(LocalDateTime.now());

        notificationMapper.insert(notification);
        
        // 通过WebSocket发送实时通知
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            notification
        );
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.findByUserId(userId);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationMapper.delete(notificationId);
    }

    @Override
    public List<Notification> getNotifications(Long userId) {
        return notificationMapper.findByUserId(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        notificationMapper.markAsRead(id, userId);
    }
} 