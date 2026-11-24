package com.escolasegura.modules.students.controller;

import com.escolasegura.modules.students.dto.GuardianLinkRequest;
import com.escolasegura.modules.students.dto.StudentCreateRequest;
import com.escolasegura.modules.students.dto.StudentResponse;
import com.escolasegura.modules.students.dto.StudentUpdateRequest;
import com.escolasegura.modules.students.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/students")
@PreAuthorize("hasAnyRole('COORDENADOR', 'SUPERVISOR')")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> searchStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Boolean active
    ) {
        List<StudentResponse> response = studentService.searchStudents(name, className, active);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateRequest request
    ) {
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<StudentResponse> updateStudentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        Boolean active = body.get("active");
        if (active == null) {
            return ResponseEntity.badRequest().build();
        }
        StudentResponse response = studentService.updateStudentStatus(id, active);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/guardians")
    public ResponseEntity<StudentResponse> linkGuardian(
            @PathVariable Long id,
            @Valid @RequestBody GuardianLinkRequest request
    ) {
        StudentResponse response = studentService.linkGuardian(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/guardians/{guardianId}")
    public ResponseEntity<StudentResponse> unlinkGuardian(
            @PathVariable Long id,
            @PathVariable Long guardianId
    ) {
        StudentResponse response = studentService.unlinkGuardian(id, guardianId);
        return ResponseEntity.ok(response);
    }
}
