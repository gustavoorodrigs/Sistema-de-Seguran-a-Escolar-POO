package com.escolasegura.modules.authorizations.dto;

import com.escolasegura.modules.authorizations.model.AuthorizedPickup;
import com.escolasegura.modules.authorizations.model.PickupStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AuthorizedPickupResponse(
        Long id,
        Long studentId,
        String studentName,
        String fullName,
        String relationship,
        String contactPhone,
        String documentNumber,
        PickupStatus status,
        String statusDescription,
        LocalDate validUntil,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static AuthorizedPickupResponse fromEntity(AuthorizedPickup ap) {
        return new AuthorizedPickupResponse(
                ap.getId(),
                ap.getStudent().getId(),
                ap.getStudent().getFullName(),
                ap.getFullName(),
                ap.getRelationship(),
                ap.getContactPhone(),
                ap.getDocumentNumber(),
                ap.getStatus(),
                ap.getStatus().getDescricao(),
                ap.getValidUntil(),
                ap.getCreatedAt(),
                ap.getUpdatedAt()
        );
    }
}
