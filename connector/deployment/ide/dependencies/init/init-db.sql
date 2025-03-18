CREATE USER participant_b WITH PASSWORD 'participant_b';
CREATE DATABASE participant_b OWNER participant_b;
GRANT ALL PRIVILEGES ON DATABASE participant_b TO participant_b;

CREATE USER keycloak WITH PASSWORD 'keycloak';
CREATE DATABASE keycloak OWNER keycloak;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
