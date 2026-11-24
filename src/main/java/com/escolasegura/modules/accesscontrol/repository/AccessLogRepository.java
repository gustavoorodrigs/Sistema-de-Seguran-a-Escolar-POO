package com.escolasegura.modules.accesscontrol.repository;

import com.escolasegura.modules.accesscontrol.model.AccessDirection;
import com.escolasegura.modules.accesscontrol.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    Optional<AccessLog> findTopByStudentIdAndDirectionAndRecordedAtAfterOrderByRecordedAtDesc(
            Long studentId,
            AccessDirection direction,
            OffsetDateTime threshold
    );

    List<AccessLog> findTop50ByOrderByRecordedAtDesc();
}