package ru.jabka.ttteam.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.jabka.ttteam.model.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class MemberMapper implements RowMapper<Member> {

    @Override
    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Member.builder()
                .teamId(rs.getLong("team_id"))
                .memberId(rs.getLong("member_id"))
                .modifiedAt(rs.getObject("modified_at", Timestamp.class).toLocalDateTime())
                .build();
    }
}