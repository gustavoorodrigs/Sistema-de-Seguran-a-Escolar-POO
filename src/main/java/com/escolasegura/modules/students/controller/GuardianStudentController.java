package com.escolasegura.modules.students.controller;

import com.escolasegura.modules.students.dto.GuardianStudentResponse;
import com.escolasegura.modules.students.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/guardian/students")
@PreAuthorize("hasRole('RESPONSAVEL')")
public class GuardianStudentController {

    private final StudentService studentService;

    public GuardianStudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<List<GuardianStudentResponse>> listMyStudents(@AuthenticationPrincipal String email) {
        List<GuardianStudentResponse> response = studentService.getStudentsForGuardian(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuardianStudentResponse> getMyStudentById(
            @PathVariable Long id,
            @AuthenticationPrincipal String email
    ) {
        GuardianStudentResponse response = studentService.getStudentForGuardianById(id, email);
        return ResponseEntity.ok(response);
    }
}
