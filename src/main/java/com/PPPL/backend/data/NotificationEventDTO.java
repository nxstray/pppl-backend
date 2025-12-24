package com.PPPL.backend.data;

import lombok.Data;
import java.io.Serializable;

@Data
public class NotificationEventDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;
    private String title;
    private String message;
    private String link;
    private String email;
    private boolean sendEmail;
    private boolean broadcastAdmin;
}