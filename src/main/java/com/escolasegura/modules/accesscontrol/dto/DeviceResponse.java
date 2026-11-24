package com.escolasegura.modules.accesscontrol.dto;

import com.escolasegura.modules.accesscontrol.model.Device;
import com.escolasegura.modules.accesscontrol.model.DeviceType;
import java.time.OffsetDateTime;

public record DeviceResponse(
        Long id,
        String name,
        DeviceType deviceType,
        String location,
        boolean active,
        String plainToken,
        OffsetDateTime createdAt
) {
    public static DeviceResponse fromEntity(Device device, String plainToken) {
        return new DeviceResponse(
                device.getId(),
                device.getName(),
                device.getDeviceType(),
                device.getLocation(),
                device.isActive(),
                plainToken,
                device.getCreatedAt()
        );
    }
}