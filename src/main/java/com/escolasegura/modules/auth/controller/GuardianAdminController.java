package com.escolasegura.modules.auth.controller;

import com.escolasegura.modules.auth.dto.GuardianAccountResponse;
import com.escolasegura.modules.auth.dto.GuardianCreateRequest;
import com.escolasegura.modules.auth.service.GuardianAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/guardians")
@PreAuthorize("hasAnyRole('COORDENADOR', 'SUPERVISOR')")
public class GuardianAdminController {

    private final GuardianAdminService guardianAdminService;

    public GuardianAdminController(GuardianAdminService guardianAdminService) {
        this.guardianAdminService = guardianAdminService;
    }

    @PostMapping
    public ResponseEntity<GuardianAccountResponse> createGuardian(@Valid @RequestBody GuardianCreateRequest request) {
        GuardianAccountResponse response = guardianAdminService.createGuardian(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GuardianAccountResponse>> listGuardians() {
        return ResponseEntity.ok(guardianAdminService.listGuardians());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<GuardianAccountResponse> updateGuardianStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        Boolean active = body.get("active");
        if (active == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(guardianAdminService.updateGuardianStatus(id, active));
    }
}