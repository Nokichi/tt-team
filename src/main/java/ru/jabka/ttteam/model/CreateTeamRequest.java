package ru.jabka.ttteam.model;

import lombok.Builder;

@Builder
public record CreateTeamRequest(
        Long id,
        String name,
        Long ownerId,
        Long createdBy
) {
}