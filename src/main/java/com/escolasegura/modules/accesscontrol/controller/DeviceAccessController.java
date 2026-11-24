package com.escolasegura.modules.accesscontrol.controller;

import com.escolasegura.modules.accesscontrol.dto.AccessEventRequest;
import com.escolasegura.modules.accesscontrol.dto.AccessEventResponse;
import com.escolasegura.modules.accesscontrol.service.AccessControlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
public class DeviceAccessController {

    private final AccessControlService accessControlService;

    public DeviceAccessController(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @PostMapping("/access")
    public ResponseEntity<AccessEventResponse> registerAccessEvent(
            Authentication authentication,
            @Valid @RequestBody AccessEventRequest request
    ) {
        Long deviceId = (Long) authentication.getPrincipal();
        AccessEventResponse response = accessControlService.processAccessEvent(deviceId, request);
        HttpStatus status = response.status().name().equals("AUTORIZADO")
                ? HttpStatus.CREATED
                : HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(response);
    }
}