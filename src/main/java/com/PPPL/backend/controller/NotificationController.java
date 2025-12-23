package com.PPPL.backend.controller;

import com.PPPL.backend.data.ApiResponse;
import com.PPPL.backend.data.NotificationDTO;
import com.PPPL.backend.service.EmailService;
import com.PPPL.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;
    
    /**
     * Get all notifications
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getAllNotifications() {
        try {
            List<NotificationDTO> notifications = notificationService.getAllNotifications();
            return ResponseEntity.ok(ApiResponse.success(notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal memuat notifikasi: " + e.getMessage()));
        }
    }
    
    /**
     * Get recent notifications (last 10)
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getRecentNotifications() {
        try {
            List<NotificationDTO> notifications = notificationService.getRecentNotifications(10);
            return ResponseEntity.ok(ApiResponse.success(notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal memuat notifikasi: " + e.getMessage()));
        }
    }
    
    /**
     * Get unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUnreadNotifications() {
        try {
            List<NotificationDTO> notifications = notificationService.getUnreadNotifications();
            return ResponseEntity.ok(ApiResponse.success(notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal memuat notifikasi: " + e.getMessage()));
        }
    }
    
    /**
     * Get unread count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        try {
            long count = notificationService.getUnreadCount();
            return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal menghitung notifikasi: " + e.getMessage()));
        }
    }
    
    @PostMapping("/test-email")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> testEmail(@RequestParam String to) {
        try {
            emailService.sendEmail(
                to,
                "TEST EMAIL SMTP",
                "<h3>SMTP berhasil! </h3><p>Email ini dikirim dari backend</p>"
            );
            return ResponseEntity.ok(ApiResponse.success("Email berhasil dikirim"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal kirim email: " + e.getMessage()));
        }
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Integer id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok(ApiResponse.success("Notifikasi ditandai sudah dibaca"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal update notifikasi: " + e.getMessage()));
        }
    }
    
    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead() {
        try {
            notificationService.markAllAsRead();
            return ResponseEntity.ok(ApiResponse.success("Semua notifikasi ditandai sudah dibaca"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal update notifikasi: " + e.getMessage()));
        }
    }
    
    /**
     * Delete notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Integer id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(ApiResponse.success("Notifikasi berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal hapus notifikasi: " + e.getMessage()));
        }
    }
    
    /**
     * Delete old read notifications (cleanup)
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> cleanupOldNotifications() {
        try {
            notificationService.deleteOldNotifications();
            return ResponseEntity.ok(ApiResponse.success("Notifikasi lama berhasil dibersihkan"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal cleanup: " + e.getMessage()));
        }
    }
}