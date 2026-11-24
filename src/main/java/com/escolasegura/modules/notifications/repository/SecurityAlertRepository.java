package com.escolasegura.modules.notifications.repository;

import com.escolasegura.modules.notifications.model.AlertStatus;
import com.escolasegura.modules.notifications.model.SecurityAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {
    List<SecurityAlert> findTop50ByOrderByCreatedAtDesc();
    long countByStatus(AlertStatus status);
}