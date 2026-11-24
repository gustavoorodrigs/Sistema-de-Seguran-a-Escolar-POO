package com.escolasegura.modules.studentportal.controller;

import com.escolasegura.modules.studentportal.dto.StudentPortalDtos.*;
import com.escolasegura.modules.studentportal.service.StudentPortalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StudentPortalController {

    private final StudentPortalService portalService;

    public StudentPortalController(StudentPortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/api/student/dashboard")
    public ResponseEntity<StudentPortalResponse> getStudentDashboard(Authentication authentication) {
        return ResponseEntity.ok(portalService.getStudentDashboard(authentication.getName()));
    }

    @GetMapping("/api/guardian/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getGuardianAnnouncements() {
        return ResponseEntity.ok(portalService.listAnnouncements());
    }

    @GetMapping("/api/admin/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAdminAnnouncements() {
        return ResponseEntity.ok(portalService.listAnnouncements());
    }

    @PostMapping("/api/admin/announcements")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @Valid @RequestBody AnnouncementCreateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portalService.createAnnouncement(request, authentication.getName()));
    }
}