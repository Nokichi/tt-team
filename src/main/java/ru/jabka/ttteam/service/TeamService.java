package ru.jabka.ttteam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.jabka.ttteam.client.UserClient;
import ru.jabka.ttteam.exception.BadRequestException;
import ru.jabka.ttteam.model.CreateTeamRequest;
import ru.jabka.ttteam.model.Team;
import ru.jabka.ttteam.model.UpdateTeamRequest;
import ru.jabka.ttteam.model.UserResponse;
import ru.jabka.ttteam.model.UserRole;
import ru.jabka.ttteam.repository.TeamRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserClient userClient;

    @Transactional(rollbackFor = Throwable.class)
    public Team create(final CreateTeamRequest request) {
        validateCreateTeamRequest(request);
        return teamRepository.insert(Team.builder()
                .name(request.name())
                .ownerId(request.ownerId())
                .updatedBy(request.createdBy())
                .build());
    }

    @Transactional(readOnly = true)
    public Team getById(final Long id) {
        return teamRepository.getById(id);
    }

    @Transactional(rollbackFor = Throwable.class)
    public Team update(final UpdateTeamRequest request) {
        validateUpdateTeamRequest(request);
        Team updates = applyUpdates(getById(request.id()), request);
        return teamRepository.update(updates);
    }

    @Transactional(readOnly = true)
    public boolean existsByOwnerId(final Long ownerId) {
        return teamRepository.existsByOwnerId(ownerId);
    }

    private void validateCreateTeamRequest(final CreateTeamRequest request) {
        ofNullable(request).orElseThrow(() -> new BadRequestException("Заполните данные для создания команды"));
        if (!StringUtils.hasText(request.name())) {
            throw new BadRequestException("Заполните название команды");
        }
        ofNullable(request.ownerId()).orElseThrow(() -> new BadRequestException("Заполните id владельца команды"));
        ofNullable(request.createdBy()).orElseThrow(() -> new BadRequestException("Заполните id создателя команды"));
        List<Long> userIds = List.of(request.ownerId(), request.createdBy());
        validateUsersAsManagers(Set.copyOf(userIds));
    }

    private void validateUpdateTeamRequest(final UpdateTeamRequest request) {
        ofNullable(request).orElseThrow(() -> new BadRequestException("Заполните данные для редактирования команды"));
        Long editor = request.updatedBy();
        ofNullable(editor).orElseThrow(() -> new BadRequestException("Заполните id редактора команды"));
        validateUsersAsManagers(Set.of(editor));
    }

    private void validateUsersAsManagers(final Set<Long> userIds) {
        Map<Long, UserRole> userRoleMap = userClient.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(UserResponse::id, UserResponse::role));
        for (Long userId : userIds) {
            if (!userRoleMap.containsKey(userId)) {
                throw new BadRequestException(String.format("Пользователь с id = %d не найден", userId));
            }
            if (!UserRole.MANAGER.equals(userRoleMap.get(userId))) {
                throw new BadRequestException(String.format("Роль пользователя id = %d не соответствует роли %s", userId, UserRole.MANAGER));
            }
        }
    }

    private Team applyUpdates(final Team existedTeam, final UpdateTeamRequest teamRequest) {
        Team.TeamBuilder teamBuilder = Team.builder().id(existedTeam.id());
        ofNullable(teamRequest.name()).ifPresentOrElse(
                teamBuilder::name,
                () -> teamBuilder.name(existedTeam.name())
        );
        ofNullable(teamRequest.ownerId()).ifPresentOrElse(ownerId -> {
                    validateUsersAsManagers(Set.of(ownerId));
                    teamBuilder.ownerId(ownerId);
                },
                () -> teamBuilder.ownerId(existedTeam.ownerId())
        );
        return teamBuilder
                .updatedBy(teamRequest.updatedBy())
                .build();
    }
}