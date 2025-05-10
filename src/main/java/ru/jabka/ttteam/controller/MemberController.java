package ru.jabka.ttteam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.jabka.ttteam.model.Member;
import ru.jabka.ttteam.model.MembersRequest;
import ru.jabka.ttteam.service.MemberService;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@Tag(name = "Члены команды")
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberService memberService;

    @PatchMapping
    @Operation(summary = "Добавить участников команды")
    public void addMembers(@RequestBody MembersRequest request) {
        memberService.addMembers(request);
    }

    @DeleteMapping
    @Operation(summary = "Удалить участников команды")
    public void deleteMembers(@RequestBody MembersRequest request) {
        memberService.deleteMembers(request);
    }

    @GetMapping("/by-team/{teamId}")
    @Operation(summary = "Получить список участников команды по ID команды")
    public Set<Member> getByTeamId(@PathVariable Long teamId) {
        return memberService.getByTeamId(teamId);
    }

    @GetMapping
    @Operation(summary = "Получить список участников команды по списку ID пользователей")
    public Set<Member> getByMemberIds(@RequestParam Set<Long> ids) {
        return memberService.getByMemberIds(ids);
    }
}