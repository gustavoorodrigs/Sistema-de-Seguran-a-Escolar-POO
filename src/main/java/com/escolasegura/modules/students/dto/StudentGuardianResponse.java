package com.escolasegura.modules.students.dto;

import com.escolasegura.modules.students.model.StudentGuardian;

public record StudentGuardianResponse(
        Long id,
        Long guardianUserId,
        String guardianName,
        String guardianEmail,
        String relationship
) {
    public static StudentGuardianResponse fromEntity(StudentGuardian sg) {
        return new StudentGuardianResponse(
                sg.getId(),
                sg.getGuardianUser().getId(),
                sg.getGuardianUser().getName(),
                sg.getGuardianUser().getEmail(),
                sg.getRelationship()
        );
    }
}
