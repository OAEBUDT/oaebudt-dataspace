#!/bin/bash
IH_API_SUPERUSER_KEY="c3VwZXItdXNlcg==.K+CKuM+8XNuEfLggseLntVljpgLnRzPMNo1WT6dWU1HUJP07l50k8AUreEIy3gcYTBn4vxzMWIg+1TDPYsxpug=="


# add participant participant-a in IdentityHub
echo
echo
echo "Create participant-a participant context in IdentityHub"
MANIFEST_PARTICIPANT_A=$(jq -n '{
                           "roles":[],
                           "serviceEndpoints":[
                               {
                               "type": "CredentialService",
                               "serviceEndpoint": "http://localhost:6102/api/credentials/v1/participants/ZGlkOndlYjpsb2NhbGhvc3QlM0E2MTAw",
                               "id": "participant-a-credentialservice-1"
                               },
                               {
                               "type": "ProtocolEndpoint",
                               "serviceEndpoint": "http://localhost:7104/api/dsp",
                               "id": "participant-a-dsp"
                               }
                           ],
                           "active": true,
                           "participantId": "did:web:localhost%3A6100",
                           "did": "did:web:localhost%3A6100",
                           "key":{
                               "keyId": "did:web:localhost%3A6100#key-1",
                               "privateKeyAlias": "did:web:localhost%3A6100#key-1",
                               "keyGeneratorParams":{
                                   "algorithm": "EC"
                               }
                           }
                       }')
curl -s --location 'http://localhost:6103/api/identity/v1alpha/participants/' \
--header 'Content-Type: application/json' \
--header "x-api-key: $IH_API_SUPERUSER_KEY" \
--data "$MANIFEST_PARTICIPANT_A"

# add participant participant-b in IdentityHub
echo
echo
echo "Create participant-b participant context in IdentityHub"
MANIFEST_PARTICIPANT_B=$(jq -n '{
                          "roles":[],
                          "serviceEndpoints":[
                              {
                                  "type": "CredentialService",
                                  "serviceEndpoint": "http://localhost:6202/api/credentials/v1/participants/ZGlkOndlYjpsb2NhbGhvc3QlM0E2MjAw",
                                  "id": "participant-b-credentialservice-1"
                              },
                              {
                                  "type": "ProtocolEndpoint",
                                  "serviceEndpoint": "http://localhost:7204/api/dsp",
                                  "id": "participant-b-dsp"
                              }
                          ],
                          "active": true,
                          "participantId": "did:web:localhost%3A6200",
                          "did": "did:web:localhost%3A6200",
                          "key":{
                              "keyId": "did:web:localhost%3A6200#key-1",
                              "privateKeyAlias": "did:web:localhost%3A6200#key-1",
                              "keyGeneratorParams":{
                                  "algorithm": "EC"
                              }
                          }
                      }')
curl -s --location 'http://localhost:6203/api/identity/v1alpha/participants/' \
--header 'Content-Type: application/json' \
--header "x-api-key: $IH_API_SUPERUSER_KEY" \
--data "$MANIFEST_PARTICIPANT_B"
