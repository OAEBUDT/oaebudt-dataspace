#!/bin/bash

# Script to generate RSA private key and certificate for participants,
# convert them to single-line JSON format, and store them as secrets in Vault

# Generate RSA private key for participants
openssl genpkey -algorithm RSA -out participant-a-private-key.pem
openssl genpkey -algorithm RSA -out participant-b-private-key.pem

# Generate corresponding certificate
openssl req -new -x509 -key participant-a-private-key.pem -out participant-a-cert.pem -days 365 -subj "/C=US/ST=Washington, D.C/L=Washington, D.C/O=OAEBUDT/OU=EDC/CN=Participant-a"
openssl req -new -x509 -key participant-b-private-key.pem -out participant-b-cert.pem -days 365 -subj "/C=US/ST=Washington, D.C/L=Washington, D.C/O=OAEBUDT/OU=EDC/CN=Participant-b"

# Convert private key to a single-line format
cat participant-a-private-key.pem | sed 's/$/\\n/' | tr -d '\n' > participant-a-private-key.pem.line
cat participant-b-private-key.pem | sed 's/$/\\n/' | tr -d '\n' > participant-b-private-key.pem.line

# Convert cert to a single-line format
cat participant-a-cert.pem | sed 's/$/\\n/' | tr -d '\n' > participant-a-cert.pem.line
cat participant-b-cert.pem | sed 's/$/\\n/' | tr -d '\n' > participant-b-cert.pem.line

# Generate JSON file with the key content
JSONFORMAT='{"content": "%s"}'
printf "$JSONFORMAT\n" "`cat participant-a-private-key.pem.line`" > participant-a-private-key.json
printf "$JSONFORMAT\n" "`cat participant-a-cert.pem.line`" > participant-a-cert.json
printf "$JSONFORMAT\n" "`cat participant-b-private-key.pem.line`" > participant-b-private-key.json
printf "$JSONFORMAT\n" "`cat participant-b-cert.pem.line`" > participant-b-cert.json

# Output success message
echo "RSA key pair in JSON format for participant-a generated successfully."

# Authenticate with the root token
export VAULT_TOKEN=${VAULT_DEV_ROOT_TOKEN_ID}

# Create a secrets
vault kv put -address=http://127.0.0.1:8200 secret/participant-a-private-key @participant-a-private-key.json
vault kv put -address=http://127.0.0.1:8200 secret/participant-b-private-key @participant-b-private-key.json
vault kv put -address=http://127.0.0.1:8200 secret/participant-a-public-key  @participant-a-cert.json
vault kv put -address=http://127.0.0.1:8200 secret/participant-b-public-key  @participant-b-cert.json

# Echo success message
echo "Private key and public address created as secrets on Vault successfully."
