CREATE TABLE if not exists participant_group (
    id VARCHAR PRIMARY KEY
);

CREATE table if not exists participant (
    id SERIAL PRIMARY KEY,
    group_id VARCHAR REFERENCES participant_group(id) ON DELETE CASCADE,
    participant_id VARCHAR NOT NULL,
    CONSTRAINT uq_group_participant UNIQUE (group_id, participant_id)
);
