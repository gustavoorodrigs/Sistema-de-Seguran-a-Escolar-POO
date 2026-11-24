package com.escolasegura.modules.authorizations.repository;

import com.escolasegura.modules.authorizations.model.PickupAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PickupAuditLogRepository extends JpaRepository<PickupAuditLog, Long> {

    List<PickupAuditLog> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<PickupAuditLog> findByAuthorizedPickupIdOrderByCreatedAtDesc(Long authorizedPickupId);
}
