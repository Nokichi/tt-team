CREATE SCHEMA tt;

CREATE TABLE tt.team
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR UNIQUE NOT NULL,
    owner_id   INT            NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by INT            NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tt.member
(
    team_id     INT REFERENCES tt.team (id),
    member_id   INT NOT NULL,
    modified_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_team_member UNIQUE (team_id, member_id)
);