package ru.jabka.ttteam.model;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String username,
        UserRole role
) {
}