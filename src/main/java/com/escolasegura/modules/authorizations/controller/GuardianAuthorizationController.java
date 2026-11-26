package com.escolasegura.modules.authorizations.controller;

import com.escolasegura.modules.authorizations.dto.AuthorizedPickupCreateRequest;
import com.escolasegura.modules.authorizations.dto.AuthorizedPickupResponse;
import com.escolasegura.modules.authorizations.service.AuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guardian/students/{studentId}/pickups")
@PreAuthorize("hasRole('RESPONSAVEL')")
public class GuardianAuthorizationController {

    private final AuthorizationService authorizationService;

    public GuardianAuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public ResponseEntity<List<AuthorizedPickupResponse>> listAuthorizedPickupsForMyStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal String guardianEmail
    ) {
        List<AuthorizedPickupResponse> response = authorizationService.getPickupsForMyStudent(studentId, guardianEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AuthorizedPickupResponse> createAuthorizedPickupForMyStudent(
            @PathVariable Long studentId,
            @Valid @RequestBody AuthorizedPickupCreateRequest request,
            @AuthenticationPrincipal String guardianEmail
    ) {
        AuthorizedPickupCreateRequest normalized = new AuthorizedPickupCreateRequest(
                studentId,
                request.fullName(),
                request.relationship(),
                request.contactPhone(),
                request.documentNumber(),
                request.validUntil()
        );
        AuthorizedPickupResponse response = authorizationService.createAuthorizedPickupForMyStudent(studentId, normalized, guardianEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{pickupId}/revoke")
    public ResponseEntity<AuthorizedPickupResponse> revokeAuthorizedPickupForMyStudent(
            @PathVariable Long studentId,
            @PathVariable Long pickupId,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal String guardianEmail
    ) {
        String reason = body != null ? body.get("reason") : null;
        AuthorizedPickupResponse response = authorizationService.revokePickupForMyStudent(studentId, pickupId, reason, guardianEmail);
        return ResponseEntity.ok(response);
    }
}