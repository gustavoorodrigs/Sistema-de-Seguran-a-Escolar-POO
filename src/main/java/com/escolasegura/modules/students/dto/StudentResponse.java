package com.escolasegura.modules.students.dto;

import com.escolasegura.modules.students.model.Student;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record StudentResponse(
        Long id,
        String fullName,
        LocalDate birthDate,
        String className,
        String contactPhone,
        String badgeCode,
        boolean active,
        List<StudentGuardianResponse> guardians,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static StudentResponse fromEntity(Student s) {
        List<StudentGuardianResponse> guardianResponses = s.getGuardians() != null
                ? s.getGuardians().stream().map(StudentGuardianResponse::fromEntity).toList()
                : List.of();

        return new StudentResponse(
                s.getId(),
                s.getFullName(),
                s.getBirthDate(),
                s.getClassName(),
                s.getContactPhone(),
                s.getBadgeCode(),
                s.isActive(),
                guardianResponses,
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
