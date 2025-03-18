CREATE USER participant_b WITH PASSWORD 'participant_b';
CREATE DATABASE participant_b OWNER participant_b;
GRANT ALL PRIVILEGES ON DATABASE participant_b TO participant_b;

CREATE USER identityhub_a WITH PASSWORD 'identityhub_a';
CREATE DATABASE identityhub_a OWNER identityhub_a;
GRANT ALL PRIVILEGES ON DATABASE identityhub_a TO identityhub_a;
