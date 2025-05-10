package ru.jabka.ttteam.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record Team(
        Long id,
        String name,
        Long ownerId,
        LocalDateTime createdAt,
        Long updatedBy,
        LocalDateTime updatedAt
) {
}