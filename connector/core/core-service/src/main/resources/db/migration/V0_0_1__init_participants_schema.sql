CREATE TABLE IF NOT EXISTS participant_group (
     id VARCHAR PRIMARY KEY,
     participants JSONB NOT NULL DEFAULT '[]'::JSONB
);
