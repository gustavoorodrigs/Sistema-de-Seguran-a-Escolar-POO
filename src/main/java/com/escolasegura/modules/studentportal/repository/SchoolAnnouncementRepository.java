package com.escolasegura.modules.studentportal.repository;

import com.escolasegura.modules.studentportal.model.SchoolAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolAnnouncementRepository extends JpaRepository<SchoolAnnouncement, Long> {
    List<SchoolAnnouncement> findTop20ByOrderByCreatedAtDesc();
}