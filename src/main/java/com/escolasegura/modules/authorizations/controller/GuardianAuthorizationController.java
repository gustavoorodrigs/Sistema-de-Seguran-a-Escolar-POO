package com.escolasegura.modules.authorizations.controller;

import com.escolasegura.modules.authorizations.dto.AuthorizedPickupResponse;
import com.escolasegura.modules.authorizations.service.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
