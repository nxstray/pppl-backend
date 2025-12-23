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

    @Autowired
    private EmailService emailService;

    /**
     * Create notification and send email
     */
    public Notification createNotificationAndSendEmail(
            String type,
            String title,
            String message,
            String link,
            String emailTujuan
    ) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setIsRead(false);
        notification.setCreatedAt(new Date());

        Notification saved = notificationRepository.save(notification);

        // kirim email
        sendNotificationEmail(emailTujuan, title, message, link);

        return saved;
    }

    /**
     * Send notification email
     */
    private void sendNotificationEmail(
            String to,
            String title,
            String message,
            String link
    ) {
        String html = """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>%s</h2>
                <p>%s</p>
                %s
                <br/>
                <small>PPPL Notification System</small>
            </body>
            </html>
        """.formatted(
                title,
                message,
                link != null
                        ? "<a href=\"" + link + "\">Buka Detail</a>"
                        : ""
        );

        emailService.sendEmail(to, title, html);
    }

    /**
     * Query notifications
     */
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    public List<NotificationDTO> getRecentNotifications(int limit) {
        return notificationRepository.findRecentNotifications(limit)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update notification as read
     */
    public void markAsRead(Integer id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    public void deleteNotification(Integer id) {
        notificationRepository.deleteById(id);
    }

    public void deleteOldNotifications() {
        Date limit = new Date(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000));
        List<Notification> old = notificationRepository.findAll()
                .stream()
                .filter(n -> n.getIsRead() && n.getCreatedAt().before(limit))
                .collect(Collectors.toList());
        notificationRepository.deleteAll(old);
    }

    /**
     * DTO Converter
     */
    private NotificationDTO convertToDTO(Notification n) {
        return new NotificationDTO(
                n.getIdNotification(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getLink(),
                n.getIsRead(),
                n.getCreatedAt()
        );
    }

    public Notification createNotification(
            String type,
            String title,
            String message,
            String link
    ) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setIsRead(false);
        notification.setCreatedAt(new Date());

        return notificationRepository.save(notification);
    }
}
