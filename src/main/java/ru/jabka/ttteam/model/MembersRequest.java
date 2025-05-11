package ru.jabka.ttteam.model;

import lombok.Builder;

import java.util.Set;

@Builder
public record MembersRequest(
        Long teamId,
        Long modifiedBy,
        Set<Long> memberIds
) {
}