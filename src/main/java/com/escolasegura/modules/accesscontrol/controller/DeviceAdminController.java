package com.escolasegura.modules.accesscontrol.controller;

import com.escolasegura.modules.accesscontrol.dto.AccessEventResponse;
import com.escolasegura.modules.accesscontrol.dto.DeviceCreateRequest;
import com.escolasegura.modules.accesscontrol.dto.DeviceResponse;
import com.escolasegura.modules.accesscontrol.service.AccessControlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/devices")
public class DeviceAdminController {

    private final AccessControlService accessControlService;

    public DeviceAdminController(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> registerDevice(@Valid @RequestBody DeviceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accessControlService.registerDevice(request));
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> listDevices() {
        return ResponseEntity.ok(accessControlService.listDevices());
    }

    @GetMapping("/access-logs")
    public ResponseEntity<List<AccessEventResponse>> listRecentAccessLogs() {
        return ResponseEntity.ok(accessControlService.listRecentAccessLogs());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeviceResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return ResponseEntity.ok(accessControlService.updateDeviceStatus(id, active));
    }
}