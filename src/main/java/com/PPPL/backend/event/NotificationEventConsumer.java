package com.PPPL.backend.event;

import com.PPPL.backend.config.RabbitMQConfig;
import com.PPPL.backend.data.NotificationEventDTO;
import com.PPPL.backend.service.EmailService;
import com.PPPL.backend.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationEventConsumer(
            NotificationService notificationService,
            EmailService emailService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.messagingTemplate = messagingTemplate;
    }

    // broadcast admin (websocket)
    @RabbitListener(queues = RabbitMQConfig.QUEUE_WS_ADMIN)
    public void handleAdminNotification(NotificationEventDTO event) {

        notificationService.createNotification(
                event.getType(),
                event.getTitle(),
                event.getMessage(),
                event.getLink()
        );

        messagingTemplate.convertAndSend(
                "/topic/admin/notifications",
                event
        );
    }

    // email only
    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void handleEmail(NotificationEventDTO event) {

        emailService.sendEmail(
                event.getEmail(),
                event.getTitle(),
                event.getMessage()
        );
    }
}
