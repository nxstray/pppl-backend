package com.PPPL.backend.service;

import com.PPPL.backend.data.NotificationDTO;
import com.PPPL.backend.model.Notification;
import com.PPPL.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Create new notification
     */
    public Notification createNotification(String type, String title, String message, String link) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setIsRead(false);
        notification.setCreatedAt(new Date());
        return notificationRepository.save(notification);
    }
    
    /**
     * Get all notifications
     */
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get unread notifications
     */
    public List<NotificationDTO> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get unread count
     */
    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }
    
    /**
     * Get recent notifications (limit 10)
     */
    public List<NotificationDTO> getRecentNotifications(int limit) {
        return notificationRepository.findRecentNotifications(limit)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Mark notification as read
     */
    public void markAsRead(Integer id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
    
    /**
     * Mark all as read
     */
    public void markAllAsRead() {
        List<Notification> unreadNotifications = notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
        unreadNotifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
    
    /**
     * Delete notification
     */
    public void deleteNotification(Integer id) {
        notificationRepository.deleteById(id);
    }
    
    /**
     * Delete old read notifications (older than 30 days)
     */
    public void deleteOldNotifications() {
        Date thirtyDaysAgo = new Date(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000));
        List<Notification> oldNotifications = notificationRepository.findAll()
            .stream()
            .filter(n -> n.getIsRead() && n.getCreatedAt().before(thirtyDaysAgo))
            .collect(Collectors.toList());
        notificationRepository.deleteAll(oldNotifications);
    }
    
    // Helper method to convert Entity to DTO
    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
            notification.getIdNotification(),
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getLink(),
            notification.getIsRead(),
            notification.getCreatedAt()
        );
    }
}