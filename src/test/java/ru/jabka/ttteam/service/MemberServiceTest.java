package ru.jabka.ttteam.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.jabka.ttteam.client.UserClient;
import ru.jabka.ttteam.exception.BadRequestException;
import ru.jabka.ttteam.model.Member;
import ru.jabka.ttteam.model.MembersRequest;
import ru.jabka.ttteam.model.UserResponse;
import ru.jabka.ttteam.model.UserRole;
import ru.jabka.ttteam.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private MemberService memberService;

    @Test
    void getByTeamId_success() {
        Long teamId = 1L;
        Set<Member> members = Set.of(Member.builder()
                .teamId(teamId)
                .memberId(2L)
                .modifiedAt(LocalDateTime.now())
                .build());
        Mockito.when(memberRepository.getByTeamId(teamId))
                .thenReturn(members.stream().toList());
        Set<Member> result = memberService.getByTeamId(teamId);
        Assertions.assertEquals(members, result);
        Mockito.verify(memberRepository).getByTeamId(teamId);
    }

    @Test
    void getByMemberIds_success() {
        Long teamId = 1L;
        Set<Member> members = Set.of(
                Member.builder()
                        .teamId(teamId)
                        .memberId(2L)
                        .modifiedAt(LocalDateTime.now())
                        .build(),
                Member.builder()
                        .teamId(teamId)
                        .memberId(3L)
                        .modifiedAt(LocalDateTime.now())
                        .build());
        Set<Long> ids = members.stream()
                .map(Member::memberId)
                .collect(Collectors.toSet());
        Mockito.when(memberRepository.getByMemberIds(ids
        )).thenReturn(members.stream().toList());
        Set<Member> result = memberService.getByMemberIds(ids);
        Assertions.assertEquals(members, result);
        Mockito.verify(memberRepository).getByMemberIds(ids);
    }

    @Test
    void addMembers_success() {
        MembersRequest request = MembersRequest.builder()
                .teamId(1L)
                .memberIds(Set.of(2L, 3L, 4L))
                .modifiedBy(5L)
                .build();
        List<Long> userIds = new ArrayList<>(request.memberIds());
        Set<UserResponse> userSet = idSetToUserSet(request.memberIds());
        userIds.add(request.modifiedBy());
        userSet.add(UserResponse.builder()
                .id(request.modifiedBy())
                .role(UserRole.MANAGER)
                .build());
        Mockito.when(userClient.getAllByIds(Set.copyOf(userIds)))
                .thenReturn(userSet);
        memberService.addMembers(request);
        Mockito.verify(userClient).getAllByIds(Set.copyOf(userIds));
        Mockito.verify(memberRepository).insert(request.teamId(), request.memberIds());
    }

    @Test
    void deleteMembers_success() {
        MembersRequest request = MembersRequest.builder()
                .teamId(1L)
                .memberIds(Set.of(2L, 3L, 4L))
                .modifiedBy(5L)
                .build();
        List<Long> userIds = new ArrayList<>(request.memberIds());
        Set<UserResponse> userSet = idSetToUserSet(request.memberIds());
        userIds.add(request.modifiedBy());
        userSet.add(UserResponse.builder()
                .id(request.modifiedBy())
                .role(UserRole.MANAGER)
                .build());
        Mockito.when(userClient.getAllByIds(Set.copyOf(userIds)))
                .thenReturn(userSet);
        memberService.deleteMembers(request);
        Mockito.verify(userClient).getAllByIds(Set.copyOf(userIds));
        Mockito.verify(memberRepository).delete(request.teamId(), request.memberIds());
    }

    @Test
    void addMembers_error_nullRequest() {
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.addMembers(null)
        );
        Assertions.assertEquals("Заполните данные для изменения состава участников команды", exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).insert(Mockito.any(), Mockito.any());
    }

    @Test
    void addMembers_error_nullTeamId() {
        MembersRequest request = MembersRequest.builder()
                .teamId(null)
                .memberIds(Set.of(2L, 3L, 4L))
                .modifiedBy(5L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.addMembers(request)
        );
        Assertions.assertEquals("Заполните id команды", exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).insert(Mockito.any(), Mockito.any());
    }

    @Test
    void addMembers_error_nullModifiedBy() {
        MembersRequest request = MembersRequest.builder()
                .teamId(1L)
                .memberIds(Set.of(2L, 3L, 4L))
                .modifiedBy(null)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.addMembers(request)
        );
        Assertions.assertEquals("Заполните id пользователя, который выполняет изменения", exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).insert(Mockito.any(), Mockito.any());
    }

    @Test
    void addMembers_error_nullMemberIds() {
        MembersRequest request = MembersRequest.builder()
                .teamId(1L)
                .memberIds(null)
                .modifiedBy(5L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.addMembers(request)
        );
        Assertions.assertEquals("Заполните список участников команды", exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).insert(Mockito.any(), Mockito.any());
    }

    @Test
    void addMembers_error_emptyMemberIds() {
        MembersRequest request = MembersRequest.builder()
                .teamId(1L)
                .memberIds(Set.of())
                .modifiedBy(5L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.addMembers(request)
        );
        Assertions.assertEquals("Заполните список участников команды", exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).insert(Mockito.any(), Mockito.any());
    }

    @Test
    void addMembers_error_modifiedByNotManager() {
        MembersRequest request = MembersRequest.builder()
                .teamId(1L)
                .memberIds(Set.of(2L, 3L, 4L))
                .modifiedBy(5L)
                .build();
        List<Long> userIds = new ArrayList<>(request.memberIds());
        Set<UserResponse> userSet = idSetToUserSet(request.memberIds());
        userIds.add(request.modifiedBy());
        userSet.add(UserResponse.builder()
                .id(request.modifiedBy())
                .role(UserRole.USER)
                .build());
        Mockito.when(userClient.getAllByIds(Set.copyOf(userIds)))
                .thenReturn(userSet);
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.addMembers(request)
        );
        Assertions.assertEquals(String.format("Роль пользователя, который выполняет изменения, не соответствует роли %s", UserRole.MANAGER), exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).insert(Mockito.any(), Mockito.any());
    }

    @Test
    void deleteMembers_error_nullRequest() {
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.deleteMembers(null)
        );
        Assertions.assertEquals("Заполните данные для изменения состава участников команды", exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).delete(Mockito.any(), Mockito.any());
    }

    @Test
    void deleteMembers_error_nullTeamId() {
        MembersRequest request = MembersRequest.builder()
                .teamId(null)
                .memberIds(Set.of(2L, 3L, 4L))
                .modifiedBy(5L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.deleteMembers(request)
        );
        Assertions.assertEquals("Заполните id команды", exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).delete(Mockito.any(), Mockito.any());
    }

    @Test
    void deleteMembers_error_nullModifiedBy() {
        MembersRequest request = MembersRequest.builder()
                .teamId(1L)
                .memberIds(Set.of(2L, 3L, 4L))
                .modifiedBy(null)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.deleteMembers(request)
        );
        Assertions.assertEquals("Заполните id пользователя, который выполняет изменения", exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).delete(Mockito.any(), Mockito.any());
    }

    @Test
    void deleteMembers_error_nullMemberIds() {
        MembersRequest request = MembersRequest.builder()
                .teamId(1L)
                .memberIds(null)
                .modifiedBy(5L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.deleteMembers(request)
        );
        Assertions.assertEquals("Заполните список участников команды", exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).delete(Mockito.any(), Mockito.any());
    }

    @Test
    void deleteMembers_error_emptyMemberIds() {
        MembersRequest request = MembersRequest.builder()
                .teamId(1L)
                .memberIds(Set.of())
                .modifiedBy(5L)
                .build();
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.deleteMembers(request)
        );
        Assertions.assertEquals("Заполните список участников команды", exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).delete(Mockito.any(), Mockito.any());
    }

    @Test
    void deleteMembers_error_modifiedByNotManager() {
        MembersRequest request = MembersRequest.builder()
                .teamId(1L)
                .memberIds(Set.of(2L, 3L, 4L))
                .modifiedBy(5L)
                .build();
        List<Long> userIds = new ArrayList<>(request.memberIds());
        Set<UserResponse> userSet = idSetToUserSet(request.memberIds());
        userIds.add(request.modifiedBy());
        userSet.add(UserResponse.builder()
                .id(request.modifiedBy())
                .role(UserRole.USER)
                .build());
        Mockito.when(userClient.getAllByIds(Set.copyOf(userIds)))
                .thenReturn(userSet);
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> memberService.deleteMembers(request)
        );
        Assertions.assertEquals(String.format("Роль пользователя, который выполняет изменения, не соответствует роли %s", UserRole.MANAGER), exception.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).delete(Mockito.any(), Mockito.any());
    }

    private Set<UserResponse> idSetToUserSet(final Set<Long> ids) {
        return ids.stream()
                .map(x -> UserResponse.builder()
                        .id(x)
                        .role(UserRole.USER)
                        .build())
                .collect(Collectors.toSet());
    }
}