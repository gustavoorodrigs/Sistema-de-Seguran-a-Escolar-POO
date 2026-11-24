package com.escolasegura.modules.authorizations.repository;

import com.escolasegura.modules.authorizations.model.EarlyExit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EarlyExitRepository extends JpaRepository<EarlyExit, Long> {

    List<EarlyExit> findByStudentIdOrderByExitTimeDesc(Long studentId);

    List<EarlyExit> findAllByOrderByExitTimeDesc();
}
