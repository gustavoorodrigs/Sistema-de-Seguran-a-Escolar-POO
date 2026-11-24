package com.escolasegura.modules.students.dto;

import com.escolasegura.modules.students.model.Student;
import java.time.LocalDate;

public record GuardianStudentResponse(
        Long id,
        String fullName,
        LocalDate birthDate,
        String className,
        boolean active
) {
    public static GuardianStudentResponse fromEntity(Student s) {
        return new GuardianStudentResponse(
                s.getId(),
                s.getFullName(),
                s.getBirthDate(),
                s.getClassName(),
                s.isActive()
        );
    }
}
