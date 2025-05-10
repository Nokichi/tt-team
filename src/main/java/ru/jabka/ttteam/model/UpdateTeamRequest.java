package ru.jabka.ttteam.model;

import lombok.Builder;

@Builder
public record UpdateTeamRequest(
        Long id,
        String name,
        Long ownerId,
        Long updatedBy
) {
}