package ru.jabka.ttteam.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.jabka.ttteam.model.Member;
import ru.jabka.ttteam.repository.mapper.MemberMapper;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MemberMapper memberMapper;

    private static final String INSERT = """
            INSERT INTO tt.member (team_id, member_id)
            VALUES (:team_id, :member_id)
            """;

    private static final String DELETE = """
            DELETE FROM tt.member
            WHERE member_id IN (:ids)
              AND team_id = :team_id
            """;

    private static final String GET_BY_TEAM_ID = """
            SELECT m.*
            FROM tt.member m
            WHERE m.team_id = :team_id
            """;

    private static final String GET_BY_MEMBER_IDS = """
            SELECT m.*
            FROM tt.member m
            WHERE m.member_id IN (:ids)
            """;

    public void insert(Long teamId, Set<Long> memberIds) {
        jdbcTemplate.batchUpdate(INSERT, memberIds.stream()
                .map(memberId -> new MapSqlParameterSource()
                        .addValue("team_id", teamId)
                        .addValue("member_id", memberId))
                .toArray(MapSqlParameterSource[]::new));
    }

    public void delete(Long teamId, Set<Long> memberIds) {
        jdbcTemplate.update(DELETE, new MapSqlParameterSource()
                .addValue("team_id", teamId)
                .addValue("ids", memberIds));
    }

    public List<Member> getByTeamId(Long id) {
        return jdbcTemplate.query(GET_BY_TEAM_ID, new MapSqlParameterSource("team_id", id), memberMapper);
    }

    public List<Member> getByMemberIds(Set<Long> ids) {
        return jdbcTemplate.query(GET_BY_MEMBER_IDS, new MapSqlParameterSource("ids", ids), memberMapper);
    }
}