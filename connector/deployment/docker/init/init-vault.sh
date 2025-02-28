#!/bin/bash

# Script to generate RSA private key and certificate for participant-a,
# convert them to single-line JSON format, and store them as secrets in Vault

# Generate RSA private key for participant-a
openssl genpkey -algorithm RSA -out participant-a-private-key.pem

# Generate corresponding certificate
openssl req -new -x509 -key participant-a-private-key.pem -out participant-a-cert.pem -days 365 -subj "/C=US/ST=Washington, D.C/L=Washington, D.C/O=OAEBUDT/OU=EDC/CN=Participant-a"

# Convert private key to a single-line format
cat participant-a-private-key.pem | sed 's/$/\\n/' | tr -d '\n' > participant-a-private-key.pem.line

# Convert cert to a single-line format
cat participant-a-cert.pem | sed 's/$/\\n/' | tr -d '\n' > participant-a-cert.pem.line

# Generate JSON file with the key content
JSONFORMAT='{"content": "%s"}'
printf "$JSONFORMAT\n" "`cat participant-a-private-key.pem.line`" > participant-a-private-key.json
printf "$JSONFORMAT\n" "`cat participant-a-cert.pem.line`" > participant-a-cert.json

# Output success message
echo "RSA key pair in JSON format for participant-a generated successfully."

# Authenticate with the root token
export VAULT_TOKEN=${VAULT_DEV_ROOT_TOKEN_ID}

# Create a secret
vault kv put -address=http://127.0.0.1:8200 secret/private-key @participant-a-private-key.json

vault kv put -address=http://127.0.0.1:8200 secret/public-key  @participant-a-cert.json

# Echo success message
echo "Private key and public address created as secrets on Vault successfully."