package ru.jabka.ttteam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.jabka.ttteam.model.CreateTeamRequest;
import ru.jabka.ttteam.model.Team;
import ru.jabka.ttteam.model.UpdateTeamRequest;
import ru.jabka.ttteam.service.TeamService;

@RestController
@Tag(name = "Команды")
@RequiredArgsConstructor
@RequestMapping("/api/v1/team")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "Создать команду")
    public Team create(@RequestBody CreateTeamRequest request) {
        return teamService.create(request);
    }

    @PatchMapping
    @Operation(summary = "Обновить данные команды")
    public Team update(@RequestBody UpdateTeamRequest request) {
        return teamService.update(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить данные команды по ID")
    public Team getById(@PathVariable Long id) {
        return teamService.getById(id);
    }

    @GetMapping("/exists")
    @Operation(summary = "Проверить, является ли пользователь по ID владельцем какой-нибудь команды")
    public Boolean existsByOwnerId(@RequestParam final Long ownerId) {
        return teamService.existsByOwnerId(ownerId);
    }
}