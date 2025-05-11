package ru.jabka.ttteam.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.jabka.ttteam.exception.BadRequestException;
import ru.jabka.ttteam.model.Team;
import ru.jabka.ttteam.repository.mapper.TeamMapper;

@Repository
@RequiredArgsConstructor
public class TeamRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TeamMapper teamMapper;

    private static final String INSERT = """
            INSERT INTO tt.team (name, owner_id, updated_by)
            VALUES (:name, :owner_id, :updated_by)
            RETURNING *
            """;

    private static final String UPDATE = """
            UPDATE TABLE tt.team
            SET name = :name, owner_id = :owner_id, updated_at = CURRENT_TIMESTAMP
            WHERE id = :id
            RETURNING *
            """;

    private static final String GET_BY_ID = """
            SELECT t.*
            FROM tt.team t
            WHERE t.id = :id
            """;

    private static final String EXISTS_BY_OWNER_ID = """
            SELECT EXISTS (
                SELECT 1
                FROM tt.team
                WHERE owner_id = :owner_id
            ) as exists_by_owner_id
            """;

    public Team insert(final Team team) {
        return jdbcTemplate.queryForObject(INSERT, teamToSql(team), teamMapper);
    }

    public Team update(final Team team) {
        return jdbcTemplate.queryForObject(UPDATE, teamToSql(team), teamMapper);
    }

    public Team getById(final Long id) {
        try {
            return jdbcTemplate.queryForObject(GET_BY_ID, new MapSqlParameterSource("id", id), teamMapper);
        } catch (EmptyResultDataAccessException ex) {
            throw new BadRequestException(String.format("Команда по ID = %d не найдена", id));
        }
    }

    public boolean existsByOwnerId(final Long ownerId) {
        return jdbcTemplate.queryForObject(EXISTS_BY_OWNER_ID, new MapSqlParameterSource("owner_id", ownerId), Boolean.class);
    }

    private MapSqlParameterSource teamToSql(final Team team) {
        return new MapSqlParameterSource()
                .addValue("id", team.id())
                .addValue("name", team.name())
                .addValue("owner_id", team.ownerId())
                .addValue("created_at", team.createdAt())
                .addValue("updated_by", team.updatedBy())
                .addValue("updated_at", team.updatedAt());
    }
}