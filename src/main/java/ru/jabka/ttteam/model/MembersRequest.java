package ru.jabka.ttteam.model;

import java.util.Set;

public record MembersRequest(
        Long teamId,
        Long modifiedBy,
        Set<Long> memberIds
) {
}