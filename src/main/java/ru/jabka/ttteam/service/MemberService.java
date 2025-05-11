package ru.jabka.ttteam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.jabka.ttteam.client.UserClient;
import ru.jabka.ttteam.exception.BadRequestException;
import ru.jabka.ttteam.model.Member;
import ru.jabka.ttteam.model.MembersRequest;
import ru.jabka.ttteam.model.UserResponse;
import ru.jabka.ttteam.model.UserRole;
import ru.jabka.ttteam.repository.MemberRepository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserClient userClient;

    @Transactional(rollbackFor = Throwable.class)
    public void addMembers(final MembersRequest request) {
        validateMembersRequest(request);
        memberRepository.insert(request.teamId(), request.memberIds());
    }

    @Transactional(rollbackFor = Throwable.class)
    public void deleteMembers(final MembersRequest request) {
        validateMembersRequest(request);
        memberRepository.delete(request.teamId(), request.memberIds());
    }

    @Transactional(readOnly = true)
    public Set<Member> getByTeamId(final Long teamId) {
        return new HashSet<>(memberRepository.getByTeamId(teamId));
    }

    @Transactional(readOnly = true)
    public Set<Member> getByMemberIds(final Set<Long> ids) {
        return new HashSet<>(memberRepository.getByMemberIds(ids));
    }

    private void validateMembersRequest(final MembersRequest request) {
        ofNullable(request).orElseThrow(() -> new BadRequestException("Заполните данные для изменения состава участников команды"));
        ofNullable(request.teamId()).orElseThrow(() -> new BadRequestException("Заполните id команды"));
        Long modifiedBy = request.modifiedBy();
        ofNullable(modifiedBy).orElseThrow(() -> new BadRequestException("Заполните id пользователя, который выполняет изменения"));
        if (CollectionUtils.isEmpty(request.memberIds())) {
            throw new BadRequestException("Заполните список участников команды");
        }
        Set<Long> userIds = new HashSet<>(request.memberIds());
        userIds.add(modifiedBy);
        Map<Long, UserRole> userRoleMap = userClient.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(UserResponse::id, UserResponse::role));
        for (Long userId : userIds) {
            if (!userRoleMap.containsKey(userId)) {
                throw new BadRequestException(String.format("Пользователь с id = %d не найден", userId));
            }
        }
        if (!UserRole.MANAGER.equals(userRoleMap.get(modifiedBy))) {
            throw new BadRequestException(String.format("Роль пользователя, который выполняет изменения, не соответствует роли %s", UserRole.MANAGER));
        }
    }
}