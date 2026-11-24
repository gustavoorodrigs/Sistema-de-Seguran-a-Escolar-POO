package com.escolasegura.modules.authorizations.repository;

import com.escolasegura.modules.authorizations.model.AuthorizedPickup;
import com.escolasegura.modules.authorizations.model.PickupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorizedPickupRepository extends JpaRepository<AuthorizedPickup, Long> {

    List<AuthorizedPickup> findByStudentIdOrderByFullNameAsc(Long studentId);

    List<AuthorizedPickup> findByStudentIdAndStatus(Long studentId, PickupStatus status);
}
