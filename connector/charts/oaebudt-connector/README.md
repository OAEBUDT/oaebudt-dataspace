# oaebudt-connector

![Version: 0.1.0](https://img.shields.io/badge/Version-0.1.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 1.16.0](https://img.shields.io/badge/AppVersion-1.16.0-informational?style=flat-square)

A Helm chart for deploying a proof-of-concept (PoC) Data Space Connector, including its dependencies, based on Eclipse EDC and supporting the Open Access EBook Usage Data Trust (OAEBUDT) initiative. The deployment consists of a single runtime that includes a Control Plane, Data Plane, Identity Hub, Federated Catalog, and a Web API component. The Web API extends the business logic to support the OAEBUDT eBook context. This chart is designed to work with an existing PostgreSQL database, an existing HashiCorp Vault instance, an existing MongoDB instance and, an existing Keycloak instance. The chart is not suitable for production use.

**Homepage:** <https://github.com/OAEBUDT/oaebudt-dataspace/tree/develop/connector/charts/oaebudt-connector>

## Maintainers

| Name | Email | Url |
| ---- | ------ | --- |
| Mohamed Khalil BELDI | <mohamedkhalilbeldi@think-it.io> |  |

## Source Code

* <https://github.com/OAEBUDT/oaebudt-dataspace/tree/develop/connector/charts/oaebudt-connector>

## Requirements

| Repository | Name | Version |
|------------|------|---------|
| https://helm.releases.hashicorp.com | vault(vault) | 0.30.0 |
| oci://registry-1.docker.io/bitnamicharts | keycloak(keycloak) | 24.5.8 |
| oci://registry-1.docker.io/bitnamicharts | mongodb(mongodb) | 16.5.0 |
| oci://registry-1.docker.io/bitnamicharts | postgresql(postgresql) | 16.6.3 |

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| affinity | object | `{}` |  |
| autoscaling.enabled | bool | `false` |  |
| autoscaling.maxReplicas | int | `100` |  |
| autoscaling.minReplicas | int | `1` |  |
| autoscaling.targetCPUUtilizationPercentage | int | `80` |  |
| catalog.crawler.initialDelay | int | `120` | Initial delay for the crawling to start. Leave blank for a random delay |
| catalog.crawler.targetsFile | string | `"/etc/dataspace/participants.json"` | File path to a JSON file containing TargetNode entries |
| catalog.nodes | list | `[]` |  |
| dcp.identityHub.credentials.configMap | string | `"{{ .Values.participant.id | lower }}-credentials"` |  |
| dcp.identityHub.credentials.mountPath | string | `"/etc/dataspace/did/credentials/{{ .Values.participant.id }}/"` |  |
| dcp.identityHub.superuserKey | string | `"c3VwZXItdXNlcg==.K+CKuM+8XNuEfLggseLntVljpgLnRzPMNo1WT6dWU1HUJP07l50k8AUreEIy3gcYTBn4vxzMWIg+1TDPYsxpug=="` | Use a base64 key |
| dcp.tls.enabled | bool | `false` |  |
| dcp.trustedIssuers | list | `[]` |  |
| endpoints.catalog.authKey | string | `"password"` |  |
| endpoints.catalog.path | string | `"/api/catalog"` |  |
| endpoints.catalog.port | int | `7102` |  |
| endpoints.consumer.path | string | `"/api/consumer"` |  |
| endpoints.consumer.port | int | `8101` |  |
| endpoints.control.path | string | `"/api/control"` |  |
| endpoints.control.port | int | `7103` |  |
| endpoints.credentials.path | string | `"/api/credentials"` |  |
| endpoints.credentials.port | int | `6102` |  |
| endpoints.default.path | string | `"/api"` |  |
| endpoints.default.port | int | `7100` |  |
| endpoints.did.path | string | `"/"` |  |
| endpoints.did.port | int | `80` |  |
| endpoints.identity.path | string | `"/api/identity"` |  |
| endpoints.identity.port | int | `6103` |  |
| endpoints.management.authKey | string | `"password"` |  |
| endpoints.management.path | string | `"/api/management"` |  |
| endpoints.management.port | int | `7105` |  |
| endpoints.presentation.path | string | `"/api/presentation"` |  |
| endpoints.presentation.port | int | `6104` |  |
| endpoints.protocol.path | string | `"/api/dsp"` |  |
| endpoints.protocol.port | int | `7104` |  |
| endpoints.public.path | string | `"/api/public"` |  |
| endpoints.public.port | int | `17100` |  |
| endpoints.report.authType | string | `"keycloak"` |  |
| endpoints.report.path | string | `"/api/report"` |  |
| endpoints.report.port | int | `8100` |  |
| endpoints.sts.path | string | `"/api/sts"` |  |
| endpoints.sts.port | int | `6106` |  |
| endpoints.version.path | string | `"/api/version"` |  |
| endpoints.version.port | int | `7106` |  |
| fullnameOverride | string | `""` |  |
| global.domain | string | `""` | Global dataspace domain (required for ingress) |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.repository | string | `"605134435349.dkr.ecr.us-east-1.amazonaws.com/oaebudt-dataspace/connector"` |  |
| image.tag | string | `"f6e5613"` |  |
| imagePullSecrets | list | `[]` |  |
| ingress.annotations."alb.ingress.kubernetes.io/group.name" | string | `"oaebudt-dataspace"` |  |
| ingress.annotations."alb.ingress.kubernetes.io/healthcheck-path" | string | `"/api/check/liveness"` |  |
| ingress.annotations."alb.ingress.kubernetes.io/healthcheck-port" | string | `"7100"` |  |
| ingress.annotations."alb.ingress.kubernetes.io/listen-ports" | string | `"[{\"HTTP\": 80}, {\"HTTPS\":443}]"` |  |
| ingress.annotations."alb.ingress.kubernetes.io/scheme" | string | `"internet-facing"` |  |
| ingress.annotations."alb.ingress.kubernetes.io/ssl-redirect" | string | `"443"` |  |
| ingress.annotations."alb.ingress.kubernetes.io/target-type" | string | `"ip"` |  |
| ingress.annotations."internet-facingalb.ingress.kubernetes.io/certificate-arn" | string | `"arn:aws:acm:us-east-1:605134435349:certificate/f5f64987-094b-4de6-9c8c-d81beb7014f7"` |  |
| ingress.className | string | `"alb"` |  |
| ingress.enabled | bool | `true` |  |
| ingress.tls | list | `[]` |  |
| keycloak.auth.adminPassword | string | `"oaebudt_keycloak"` |  |
| keycloak.auth.adminUser | string | `"admin"` |  |
| keycloak.externalDatabase.existingSecret | string | `"keycloak-db"` |  |
| keycloak.externalDatabase.existingSecretDatabaseKey | string | `"database"` |  |
| keycloak.externalDatabase.existingSecretHostKey | string | `"host"` |  |
| keycloak.externalDatabase.existingSecretPasswordKey | string | `"password"` |  |
| keycloak.externalDatabase.existingSecretPortKey | string | `"port"` |  |
| keycloak.externalDatabase.existingSecretUserKey | string | `"user"` |  |
| keycloak.extraEnvVars[0].name | string | `"KEYCLOAK_EXTRA_ARGS"` |  |
| keycloak.extraEnvVars[0].value | string | `"--import-realm"` |  |
| keycloak.extraVolumeMounts[0].mountPath | string | `"/opt/bitnami/keycloak/data/import"` |  |
| keycloak.extraVolumeMounts[0].name | string | `"realm"` |  |
| keycloak.extraVolumeMounts[0].readOnly | bool | `true` |  |
| keycloak.extraVolumes[0].configMap.items[0].key | string | `"participant-realm.json"` |  |
| keycloak.extraVolumes[0].configMap.items[0].path | string | `""` |  |
| keycloak.extraVolumes[0].configMap.name | string | `"participant-realm"` |  |
| keycloak.extraVolumes[0].name | string | `"realm"` |  |
| keycloak.importRealm.enabled | bool | `true` |  |
| keycloak.initContainers[0].command[0] | string | `"sh"` |  |
| keycloak.initContainers[0].command[1] | string | `"-c"` |  |
| keycloak.initContainers[0].command[2] | string | `"echo \"Installing dependencies...\"\napk add --no-cache curl netcat-openbsd\n\necho \"Waiting for PostgreSQL...\"\nuntil nc -z {{ .Release.Name }}-postgresql 5432; do\necho \"waiting for postgres...\";\nsleep 2;\ndone\necho \"PostgreSQL is ready.\"\n"` |  |
| keycloak.initContainers[0].image | string | `"alpine:3.21.3"` |  |
| keycloak.initContainers[0].name | string | `"wait-for-postgresql"` |  |
| keycloak.install | bool | `true` | Switch to enable or disable the Keycloak helm chart |
| keycloak.participantRealm.accessTokenLifespan | int | `3600` |  |
| keycloak.participantRealm.clientSecret | string | `"bKE6qbAz8Eugwvloklc03yikLDXHO2Qs"` |  |
| keycloak.participantRealm.realm | string | `""` |  |
| keycloak.participantRealm.userEmail | string | `""` |  |
| keycloak.participantRealm.userFirstName | string | `""` |  |
| keycloak.participantRealm.userLastName | string | `""` |  |
| keycloak.participantRealm.userPassword | string | `""` |  |
| keycloak.participantRealm.userRealmRoles | list | `[]` |  |
| keycloak.participantRealm.username | string | `""` |  |
| keycloak.postgresql.enabled | bool | `false` |  |
| keycloak.resources.requests.cpu | string | `"100m"` |  |
| keycloak.resources.requests.memory | string | `"256Mi"` |  |
| livenessProbe.enabled | bool | `true` |  |
| livenessProbe.failureThreshold | int | `6` |  |
| livenessProbe.initialDelaySeconds | int | `30` |  |
| livenessProbe.periodSeconds | int | `10` |  |
| livenessProbe.successThreshold | int | `1` |  |
| livenessProbe.timeoutSeconds | int | `5` |  |
| mongodb.auth.databases[0] | string | `"oaebudt"` |  |
| mongodb.auth.passwords[0] | string | `"oaebudt_report"` |  |
| mongodb.auth.rootPassword | string | `"oaebudt_root"` |  |
| mongodb.auth.rootUser | string | `"root"` |  |
| mongodb.auth.usernames[0] | string | `"oaebudt_report"` |  |
| mongodb.install | bool | `true` | Switch to enable or disable the MongoDB helm chart |
| mongodb.persistence.size | string | `"2Gi"` |  |
| mongodb.resources.requests.cpu | string | `"100m"` |  |
| mongodb.resources.requests.memory | string | `"256Mi"` |  |
| nameOverride | string | `""` |  |
| nodeSelector | object | `{}` |  |
| participant.did | string | `""` |  |
| participant.id | string | `""` |  |
| podAnnotations | object | `{}` |  |
| podLabels | object | `{}` |  |
| podSecurityContext | object | `{}` |  |
| postgresql.auth.database | string | `"oaebudt_connector"` | Maximum name length is 31 characters by default |
| postgresql.auth.password | string | `"oaebudt_connector"` |  |
| postgresql.auth.postgresPassword | string | `"oaebudt_postgres"` |  |
| postgresql.auth.username | string | `"oaebudt_connector"` | Maximum name length is 31 characters by default |
| postgresql.install | bool | `true` | Switch to enable or disable the PostgreSQL helm chart |
| postgresql.primary.initdb.password | string | `"oaebudt_postgres"` |  |
| postgresql.primary.initdb.scripts."00_init_extensions.sql" | string | `"CREATE USER keycloak WITH PASSWORD 'keycloak';\nCREATE DATABASE keycloak OWNER keycloak;\nGRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;\n"` |  |
| postgresql.primary.initdb.user | string | `"postgres"` |  |
| postgresql.primary.persistence.size | string | `"2Gi"` |  |
| postgresql.schema.autoCreate | bool | `true` | Enable auto-creation of the schema on boot |
| readinessProbe.enabled | bool | `true` |  |
| readinessProbe.failureThreshold | int | `6` |  |
| readinessProbe.initialDelaySeconds | int | `30` |  |
| readinessProbe.periodSeconds | int | `10` |  |
| readinessProbe.successThreshold | int | `1` |  |
| readinessProbe.timeoutSeconds | int | `5` |  |
| replicaCount | int | `1` |  |
| resources.limits.cpu | string | `"300m"` |  |
| resources.limits.memory | string | `"1536Mi"` |  |
| resources.requests.cpu | string | `"100m"` |  |
| resources.requests.memory | string | `"256Mi"` |  |
| securityContext.runAsUser | int | `0` |  |
| service.annotations | object | `{}` |  |
| service.labels | object | `{}` |  |
| service.type | string | `"ClusterIP"` |  |
| serviceAccount.annotations | object | `{}` |  |
| serviceAccount.automount | bool | `true` |  |
| serviceAccount.create | bool | `false` |  |
| serviceAccount.imagePullSecrets | list | `[]` |  |
| serviceAccount.name | string | `""` |  |
| tolerations | list | `[]` |  |
| vault.hashicorp.healthCheck.enabled | bool | `true` |  |
| vault.hashicorp.healthCheck.standbyOk | bool | `true` |  |
| vault.hashicorp.paths.folder | string | `""` |  |
| vault.hashicorp.paths.health | string | `"/v1/sys/health"` |  |
| vault.hashicorp.paths.secret | string | `"/v1/secret"` |  |
| vault.hashicorp.timeout | int | `30` |  |
| vault.hashicorp.tokenSecret | string | `"{{ .Release.Name }}-vault-token"` |  |
| vault.hashicorp.tokenSecretKey | string | `"root-token"` |  |
| vault.hashicorp.url | string | `"http://{{ .Release.Name }}-vault:8200"` |  |
| vault.injector.enabled | bool | `false` |  |
| vault.install | bool | `true` | Switch to enable or disable the HashiCorp Vault helm chart |
| vault.server.dataStorage.size | string | `"2Gi"` |  |
| vault.server.standalone.enabled | bool | `true` |  |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.14.2](https://github.com/norwoodj/helm-docs/releases/v1.14.2)
