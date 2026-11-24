package com.escolasegura.modules.notifications.repository;

import com.escolasegura.modules.notifications.model.GuardianNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuardianNotificationRepository extends JpaRepository<GuardianNotification, Long> {
    List<GuardianNotification> findTop30ByGuardianUserEmailOrderByCreatedAtDesc(String email);
    long countByGuardianUserEmailAndReadFalse(String email);
    Optional<GuardianNotification> findByIdAndGuardianUserEmail(Long id, String email);
    List<GuardianNotification> findByGuardianUserEmailAndReadFalse(String email);
}