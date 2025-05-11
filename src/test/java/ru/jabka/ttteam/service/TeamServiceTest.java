package ru.jabka.ttteam.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.jabka.ttteam.client.UserClient;
import ru.jabka.ttteam.exception.BadRequestException;
import ru.jabka.ttteam.model.CreateTeamRequest;
import ru.jabka.ttteam.model.Team;
import ru.jabka.ttteam.model.UpdateTeamRequest;
import ru.jabka.ttteam.model.UserResponse;
import ru.jabka.ttteam.model.UserRole;
import ru.jabka.ttteam.repository.TeamRepository;

import java.util.Set;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private TeamService teamService;

    @Test
    void create_success() {
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("Name")
                .ownerId(2L)
                .createdBy(3L)
                .build();
        Set<Long> userIds = Set.of(request.ownerId(), request.createdBy());
        Mockito.when(userClient.getAllByIds(userIds))
                .thenReturn(idSetToManagerSet(userIds));
        Team team = Team.builder()
                .name(request.name())
                .ownerId(request.ownerId())
                .updatedBy(request.createdBy())
                .build();
        Mockito.when(teamRepository.insert(team)).thenReturn(team);
        Team result = teamService.create(request);
        Assertions.assertEquals(team, result);
        Mockito.verify(teamRepository).insert(team);
        Mockito.verify(userClient).getAllByIds(userIds);
    }

    @Test
    void update_success() {
        UpdateTeamRequest request = UpdateTeamRequest.builder()
                .id(1L)
                .name("name")
                .ownerId(2L)
                .updatedBy(3L)
                .build();
        Team team = Team.builder()
                .id(request.id())
                .name(request.name())
                .ownerId(request.ownerId())
                .updatedBy(request.updatedBy())
                .build();
        Mockito.when(teamRepository.getById(team.id())).thenReturn(team);
        Set<Long> ownerIdSet = Set.of(request.ownerId());
        Mockito.when(userClient.getAllByIds(ownerIdSet))
                .thenReturn(idSetToManagerSet(ownerIdSet));
        Set<Long> editorIdSet = Set.of(request.updatedBy());
        Mockito.when(userClient.getAllByIds(editorIdSet))
                .thenReturn(idSetToManagerSet(editorIdSet));
        Mockito.when(teamRepository.update(team)).thenReturn(team);
        Team result = teamService.update(request);
        Assertions.assertEquals(team, result);
        Mockito.verify(teamRepository).update(team);
        InOrder inOrder = Mockito.inOrder(userClient);
        inOrder.verify(userClient).getAllByIds(editorIdSet);
        inOrder.verify(userClient).getAllByIds(ownerIdSet);
    }

    @Test
    void getById_success() {
        Team team = Team.builder()
                .id(1L)
                .name("Name")
                .ownerId(2L)
                .updatedBy(3L)
                .build();
        Mockito.when(teamRepository.getById(team.id())).thenReturn(team);
        Team result = teamService.getById(team.id());
        Assertions.assertEquals(team, result);
        Mockito.verify(teamRepository).getById(team.id());
    }

    @Test
    void existsByOwnerId() {
        Long ownerId = 1L;
        Mockito.when(teamRepository.existsByOwnerId(ownerId)).thenReturn(true);
        boolean result = teamService.existsByOwnerId(ownerId);
        Assertions.assertTrue(result);
        Mockito.verify(teamRepository).existsByOwnerId(ownerId);
    }

    @Test
    void create_error_nullRequest() {
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.create(null)
        );
        Assertions.assertEquals("Заполните данные для создания команды", exception.getMessage());
        Mockito.verify(teamRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_nullName() {
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name(null)
                .ownerId(2L)
                .createdBy(3L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.create(request)
        );
        Assertions.assertEquals("Заполните название команды", exception.getMessage());
        Mockito.verify(teamRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_nullOwnerId() {
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("name")
                .ownerId(null)
                .createdBy(3L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.create(request)
        );
        Assertions.assertEquals("Заполните id владельца команды", exception.getMessage());
        Mockito.verify(teamRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_nullCreatedBy() {
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("name")
                .ownerId(1L)
                .createdBy(null)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.create(request)
        );
        Assertions.assertEquals("Заполните id создателя команды", exception.getMessage());
        Mockito.verify(teamRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_ownerIdNotManager() {
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("name")
                .ownerId(1L)
                .createdBy(2L)
                .build();
        Set<Long> userIds = Set.of(request.ownerId(), request.createdBy());
        UserResponse ownerResponse = UserResponse.builder()
                .id(request.ownerId())
                .role(UserRole.USER)
                .build();
        UserResponse createdByResponse = UserResponse.builder()
                .id(request.createdBy())
                .role(UserRole.MANAGER)
                .build();
        Mockito.when(userClient.getAllByIds(userIds))
                .thenReturn(Set.of(ownerResponse, createdByResponse));
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.create(request)
        );
        Assertions.assertEquals(String.format("Роль пользователя id = %d не соответствует роли %s", request.ownerId(), UserRole.MANAGER), exception.getMessage());
        Mockito.verify(teamRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void create_error_createdByNotManager() {
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("name")
                .ownerId(1L)
                .createdBy(2L)
                .build();
        Set<Long> userIds = Set.of(request.ownerId(), request.createdBy());
        UserResponse ownerResponse = UserResponse.builder()
                .id(request.ownerId())
                .role(UserRole.MANAGER)
                .build();
        UserResponse createdByResponse = UserResponse.builder()
                .id(request.createdBy())
                .role(UserRole.USER)
                .build();
        Mockito.when(userClient.getAllByIds(userIds))
                .thenReturn(Set.of(ownerResponse, createdByResponse));
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.create(request)
        );
        Assertions.assertEquals(String.format("Роль пользователя id = %d не соответствует роли %s", request.createdBy(), UserRole.MANAGER), exception.getMessage());
        Mockito.verify(teamRepository, Mockito.never()).insert(Mockito.any());
    }

    @Test
    void update_error_nullRequest() {
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.update(null)
        );
        Assertions.assertEquals("Заполните данные для редактирования команды", exception.getMessage());
        Mockito.verify(teamRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_nullUpdatedBy() {
        UpdateTeamRequest request = UpdateTeamRequest.builder()
                .id(1L)
                .updatedBy(null)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.update(request)
        );
        Assertions.assertEquals("Заполните id редактора команды", exception.getMessage());
        Mockito.verify(teamRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_updatedByNotManager() {
        UpdateTeamRequest request = UpdateTeamRequest.builder()
                .id(1L)
                .updatedBy(2L)
                .build();
        UserResponse updatedByResponse = UserResponse.builder()
                .id(request.updatedBy())
                .role(UserRole.USER)
                .build();
        Set<Long> userIds = Set.of(request.updatedBy());
        Mockito.when(userClient.getAllByIds(userIds))
                .thenReturn(Set.of(updatedByResponse));
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.update(request)
        );
        Assertions.assertEquals(String.format("Роль пользователя id = %d не соответствует роли %s", request.updatedBy(), UserRole.MANAGER), exception.getMessage());
        Mockito.verify(teamRepository, Mockito.never()).update(Mockito.any());
    }

    @Test
    void update_error_ownerNotManager() {
        UpdateTeamRequest request = UpdateTeamRequest.builder()
                .id(1L)
                .ownerId(2L)
                .updatedBy(3L)
                .build();
        UserResponse updatedByResponse = UserResponse.builder()
                .id(request.updatedBy())
                .role(UserRole.MANAGER)
                .build();
        Mockito.when(userClient.getAllByIds(Set.of(request.updatedBy())))
                .thenReturn(Set.of(updatedByResponse));
        UserResponse ownerResponse = UserResponse.builder()
                .id(request.ownerId())
                .role(UserRole.USER)
                .build();
        Mockito.when(userClient.getAllByIds(Set.of(request.ownerId())))
                .thenReturn(Set.of(ownerResponse));
        Team team = Team.builder()
                .id(request.id())
                .name("name")
                .build();
        Mockito.when(teamRepository.getById(team.id())).thenReturn(team);
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.update(request)
        );
        Assertions.assertEquals(String.format("Роль пользователя id = %d не соответствует роли %s", request.ownerId(), UserRole.MANAGER), exception.getMessage());
        Mockito.verify(teamRepository, Mockito.never()).update(Mockito.any());
    }

    private Set<UserResponse> idSetToManagerSet(final Set<Long> ids) {
        return ids.stream()
                .map(x -> UserResponse.builder()
                        .id(x)
                        .role(UserRole.MANAGER)
                        .build())
                .collect(Collectors.toSet());
    }
}