package com.escolasegura.modules.notifications.controller;

import com.escolasegura.modules.notifications.dto.AlertResolveRequest;
import com.escolasegura.modules.notifications.dto.SecurityAlertResponse;
import com.escolasegura.modules.notifications.service.NotificationAndAlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/alerts")
@PreAuthorize("hasAnyRole('COORDENADOR', 'SUPERVISOR')")
public class SecurityAlertController {

    private final NotificationAndAlertService service;

    public SecurityAlertController(NotificationAndAlertService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SecurityAlertResponse>> listAlerts() {
        return ResponseEntity.ok(service.listSecurityAlerts());
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<SecurityAlertResponse> resolveAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertResolveRequest request,
            Authentication auth
    ) {
        return ResponseEntity.ok(service.resolveAlert(id, request.resolutionNotes(), auth.getName()));
    }
}