package com.PPPL.backend.event;

import com.PPPL.backend.config.RabbitMQConfig;
import com.PPPL.backend.data.NotificationEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public NotificationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publish notifikasi untuk admin (via WebSocket)
     */
    public void publishAdminNotification(
            String type,
            String title,
            String message,
            String link
    ) {
        NotificationEventDTO event = new NotificationEventDTO();
        event.setType(type);
        event.setTitle(title);
        event.setMessage(message);
        event.setLink(link);
        event.setBroadcastAdmin(true);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NOTIFICATION,
                    RabbitMQConfig.ROUTING_KEY_ADMIN,
                    event
            );
            log.info("Published to RabbitMQ (ADMIN): {}", title);
        } catch (Exception e) {
            log.error("Failed to publish admin notification: {}", e.getMessage());
        }
    }

    /**
     * Publish email notifikasi
     */
    public void publishEmailNotification(
            String email,
            String title,
            String message
    ) {
        NotificationEventDTO event = new NotificationEventDTO();
        event.setEmail(email);
        event.setTitle(title);
        event.setMessage(message);
        event.setSendEmail(true);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NOTIFICATION,
                    RabbitMQConfig.ROUTING_KEY_EMAIL,
                    event
            );
            log.info("Published to RabbitMQ (EMAIL): {}", email);
        } catch (Exception e) {
            log.error("Failed to publish email notification: {}", e.getMessage());
        }
    }

    /**
     * Publish both admin broadcast + email
     */
    public void publishFullNotification(
            String type,
            String title,
            String message,
            String link,
            String email
    ) {
        // Admin notification
        publishAdminNotification(type, title, message, link);

        // Email notification
        if (email != null && !email.isEmpty()) {
            publishEmailNotification(email, title, buildEmailHtml(title, message, link));
        }
    }

    /**
     * Build HTML email template
     */
    private String buildEmailHtml(String title, String message, String link) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>%s</h2>
                <p>%s</p>
                %s
                <br/>
                <small>PPPL Notification System</small>
            </body>
            </html>
            """,
            title,
            message,
            link != null ? "<a href=\"" + link + "\">Buka Detail</a>" : ""
        );
    }
}