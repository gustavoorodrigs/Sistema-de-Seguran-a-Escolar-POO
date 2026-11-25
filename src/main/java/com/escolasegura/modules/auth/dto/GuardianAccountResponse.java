package com.escolasegura.modules.auth.dto;

import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.students.model.StudentGuardian;

import java.util.List;

public record GuardianAccountResponse(
        Long id,
        String name,
        String email,
        boolean active,
        List<LinkedStudentSummary> linkedStudents
) {
    public record LinkedStudentSummary(
            Long studentId,
            String studentName,
            String className,
            String relationship
    ) {
    }

    public static GuardianAccountResponse fromEntity(User user, List<StudentGuardian> links) {
        List<LinkedStudentSummary> summaries = links.stream()
                .map(sg -> new LinkedStudentSummary(
                        sg.getStudent().getId(),
                        sg.getStudent().getFullName(),
                        sg.getStudent().getClassName(),
                        sg.getRelationship()
                ))
                .toList();

        return new GuardianAccountResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isActive(),
                summaries
        );
    }
}