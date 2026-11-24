package com.escolasegura.modules.notifications.controller;

import com.escolasegura.modules.notifications.dto.GuardianNotificationResponse;
import com.escolasegura.modules.notifications.service.NotificationAndAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class GuardianNotificationController {

    private final NotificationAndAlertService service;

    public GuardianNotificationController(NotificationAndAlertService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<GuardianNotificationResponse>> listMyNotifications(Authentication auth) {
        return ResponseEntity.ok(service.listGuardianNotifications(auth.getName()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication auth) {
        return ResponseEntity.ok(Map.of("unreadCount", service.countUnreadForGuardian(auth.getName())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, Authentication auth) {
        service.markAsRead(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Authentication auth) {
        service.markAllAsRead(auth.getName());
        return ResponseEntity.noContent().build();
    }
}
