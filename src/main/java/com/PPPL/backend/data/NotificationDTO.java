package com.PPPL.backend.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Integer idNotification;
    private String type;
    private String title;
    private String message;
    private String link;
    private Boolean isRead;
    private Date createdAt;
}