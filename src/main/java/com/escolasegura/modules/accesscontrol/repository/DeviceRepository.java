package com.escolasegura.modules.accesscontrol.repository;

import com.escolasegura.modules.accesscontrol.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByTokenHashAndActiveTrue(String tokenHash);
}