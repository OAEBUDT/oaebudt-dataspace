CREATE USER connector_b WITH PASSWORD 'connector_b';
CREATE DATABASE connector_b OWNER connector_b;
GRANT ALL PRIVILEGES ON DATABASE connector_b TO connector_b;

CREATE USER identityhub_a WITH PASSWORD 'identityhub_a';
CREATE DATABASE identityhub_a OWNER identityhub_a;
GRANT ALL PRIVILEGES ON DATABASE identityhub_a TO identityhub_a;

CREATE USER identityhub_b WITH PASSWORD 'identityhub_b';
CREATE DATABASE identityhub_b OWNER identityhub_b;
GRANT ALL PRIVILEGES ON DATABASE identityhub_b TO identityhub_b;

CREATE USER issuerservice WITH PASSWORD 'issuerservice';
CREATE DATABASE issuerservice OWNER issuerservice;
GRANT ALL PRIVILEGES ON DATABASE issuerservice TO issuerservice;
