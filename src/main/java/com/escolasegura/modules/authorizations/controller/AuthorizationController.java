package com.escolasegura.modules.authorizations.controller;

import com.escolasegura.modules.authorizations.dto.*;
import com.escolasegura.modules.authorizations.service.AuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/authorizations")
@PreAuthorize("hasAnyRole('COORDENADOR', 'SUPERVISOR')")
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/pickups")
    public ResponseEntity<AuthorizedPickupResponse> createPickup(
            @Valid @RequestBody AuthorizedPickupCreateRequest request,
            @AuthenticationPrincipal String operatorEmail
    ) {
        AuthorizedPickupResponse response = authorizationService.createAuthorizedPickup(request, operatorEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/pickups/{id}/status")
    public ResponseEntity<AuthorizedPickupResponse> updatePickupStatus(
            @PathVariable Long id,
            @Valid @RequestBody AuthorizedPickupStatusUpdateRequest request,
            @AuthenticationPrincipal String operatorEmail
    ) {
        AuthorizedPickupResponse response = authorizationService.updatePickupStatus(id, request, operatorEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/students/{studentId}/pickups")
    public ResponseEntity<List<AuthorizedPickupResponse>> listPickups(@PathVariable Long studentId) {
        List<AuthorizedPickupResponse> response = authorizationService.listPickupsByStudent(studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/students/{studentId}/audit")
    public ResponseEntity<List<PickupAuditLogResponse>> listAuditLogs(@PathVariable Long studentId) {
        List<PickupAuditLogResponse> response = authorizationService.listAuditLogsByStudent(studentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/early-exits")
    public ResponseEntity<EarlyExitResponse> registerEarlyExit(
            @Valid @RequestBody EarlyExitCreateRequest request,
            @AuthenticationPrincipal String operatorEmail
    ) {
        EarlyExitResponse response = authorizationService.registerEarlyExit(request, operatorEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/early-exits")
    public ResponseEntity<List<EarlyExitResponse>> listEarlyExits(
            @RequestParam(required = false) Long studentId
    ) {
        List<EarlyExitResponse> response = authorizationService.listEarlyExits(studentId);
        return ResponseEntity.ok(response);
    }
}
