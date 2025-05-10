package ru.jabka.ttteam.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.jabka.ttteam.model.Team;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class TeamMapper implements RowMapper<Team> {

    @Override
    public Team mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Team.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .ownerId(rs.getLong("owner_id"))
                .createdAt(rs.getObject("created_at", Timestamp.class).toLocalDateTime())
                .updatedBy(rs.getLong("updated_by"))
                .updatedAt(rs.getObject("updated_at", Timestamp.class).toLocalDateTime())
                .build();
    }
}