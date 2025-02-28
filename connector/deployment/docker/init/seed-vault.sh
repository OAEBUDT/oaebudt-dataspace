#!/bin/bash

# Script to generate RSA private key and certificate for a participant,
# convert them to single-line JSON format, and store them as secrets in Vault

# Check if participant name was provided
if [ -z "$1" ]; then
  echo "Usage: $0 <participant-name>"
  echo "Example: $0 participant-a"
  exit 1
fi

# Set participant name from command line argument
PARTICIPANT_NAME="$1"

echo "Generating keys for ${PARTICIPANT_NAME}..."

# Generate RSA private key for participant
openssl genpkey -algorithm RSA -out ${PARTICIPANT_NAME}-private-key.pem

# Generate corresponding certificate
openssl req -new -x509 -key ${PARTICIPANT_NAME}-private-key.pem -out ${PARTICIPANT_NAME}-cert.pem -days 365 -subj "/C=US/ST=Washington, D.C/L=Washington, D.C/O=OAEBUDT/OU=EDC/CN=${PARTICIPANT_NAME}"

# Convert private key to a single-line format
cat ${PARTICIPANT_NAME}-private-key.pem | sed 's/$/\\n/' | tr -d '\n' > ${PARTICIPANT_NAME}-private-key.pem.line

# Convert cert to a single-line format
cat ${PARTICIPANT_NAME}-cert.pem | sed 's/$/\\n/' | tr -d '\n' > ${PARTICIPANT_NAME}-cert.pem.line

# Generate JSON file with the key content
JSONFORMAT='{"content": "%s"}'
printf "$JSONFORMAT\n" "$(cat ${PARTICIPANT_NAME}-private-key.pem.line)" > ${PARTICIPANT_NAME}-private-key.json
printf "$JSONFORMAT\n" "$(cat ${PARTICIPANT_NAME}-cert.pem.line)" > ${PARTICIPANT_NAME}-cert.json

# Output success message
echo "RSA key pair in JSON format for ${PARTICIPANT_NAME} generated successfully."

# Authenticate with the root token
export VAULT_TOKEN=${VAULT_DEV_ROOT_TOKEN_ID}

# Define secret paths with participant name
PRIVATE_KEY_PATH="secret/${PARTICIPANT_NAME}/private-key"
PUBLIC_KEY_PATH="secret/${PARTICIPANT_NAME}/public-key"

# Create secrets in Vault
vault kv put -address=http://127.0.0.1:8200 ${PRIVATE_KEY_PATH} @${PARTICIPANT_NAME}-private-key.json
vault kv put -address=http://127.0.0.1:8200 ${PUBLIC_KEY_PATH} @${PARTICIPANT_NAME}-cert.json

# Echo success message
echo "Private key and public key created as secrets on Vault successfully for ${PARTICIPANT_NAME}."