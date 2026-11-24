package com.escolasegura.modules.auth.dto;

import com.escolasegura.modules.auth.model.Role;
import com.escolasegura.modules.auth.model.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        String roleDescription,
        boolean active
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getRole().getDescricao(),
                user.isActive()
        );
    }
}
