# oaebudt-connector

![Version: 0.1.0](https://img.shields.io/badge/Version-0.1.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 1.16.0](https://img.shields.io/badge/AppVersion-1.16.0-informational?style=flat-square)

A Helm chart for deploying a proof-of-concept (PoC) Data Space Connector, including its dependencies, based on Eclipse EDC and supporting the Open Access EBook Usage Data Trust (OAEBUDT) initiative. The deployment consists of a single runtime that includes a Control Plane, Data Plane, Identity Hub, Federated Catalog, and a Web API component. The Web API extends the business logic to support the OAEBUDT eBook context. This chart is designed to work with an existing PostgreSQL database and an existing HashiCorp Vault instance.

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
| endpoints.sts.path | string | `"/api/sts"` |  |
| endpoints.sts.port | int | `6106` |  |
| endpoints.version.path | string | `"/api/version"` |  |
| endpoints.version.port | int | `7106` |  |
| fullnameOverride | string | `""` |  |
| global.domain | string | `""` | Global dataspace domain (required for ingress) |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.repository | string | `"605134435349.dkr.ecr.us-east-1.amazonaws.com/oaebudt-dataspace/connector"` |  |
| image.tag | string | `""` |  |
| imagePullSecrets | list | `[]` |  |
| ingress.annotations | list | `[]` |  |
| ingress.className | string | `""` |  |
| ingress.enabled | string | `"enable"` |  |
| ingress.tls | list | `[]` |  |
| livenessProbe.enabled | bool | `true` |  |
| livenessProbe.failureThreshold | int | `6` |  |
| livenessProbe.initialDelaySeconds | int | `30` |  |
| livenessProbe.periodSeconds | int | `10` |  |
| livenessProbe.successThreshold | int | `1` |  |
| livenessProbe.timeoutSeconds | int | `5` |  |
| nameOverride | string | `""` |  |
| nodeSelector | object | `{}` |  |
| participant.did | string | `""` |  |
| participant.id | string | `""` |  |
| podAnnotations | object | `{}` |  |
| podLabels | object | `{}` |  |
| podSecurityContext | object | `{}` |  |
| postgresql.auth.database | string | `"oaebudt_connector"` | Maximum name length is 31 characters by default |
| postgresql.auth.password | string | `"oaebudt_connector"` |  |
| postgresql.auth.username | string | `"oaebudt_connector"` | Maximum name length is 31 characters by default |
| postgresql.install | bool | `true` | Switch to enable or disable the PostgreSQL helm chart |
| postgresql.schema.autoCreate | bool | `true` | Enable auto-creation of the schema on boot |
| readinessProbe.enabled | bool | `true` |  |
| readinessProbe.failureThreshold | int | `6` |  |
| readinessProbe.initialDelaySeconds | int | `30` |  |
| readinessProbe.periodSeconds | int | `10` |  |
| readinessProbe.successThreshold | int | `1` |  |
| readinessProbe.timeoutSeconds | int | `5` |  |
| replicaCount | int | `1` |  |
| resources.limits.cpu | float | `1.5` |  |
| resources.limits.memory | string | `"1536Mi"` |  |
| resources.requests.cpu | string | `"500m"` |  |
| resources.requests.memory | string | `"1024Mi"` |  |
| securityContext | object | `{}` |  |
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
| vault.server.dataStorage.enabled | bool | `false` |  |
| vault.server.serviceAccount.name | string | `"oaebudt-ds-vault"` |  |
| vault.server.standalone.config | string | `"ui = true \nlistener \"tcp\" {\n  tls_disable = 1\n  address = \"[::]:8200\"\n  cluster_address = \"[::]:8201\"\n}\nstorage \"dynamodb\" {\n  region         = \"us-east-1\"\n  table          = \"dev-oaebudt-ds-vault-dynamodb-table\"\n  ha_enabled     = \"false\"\n}\nseal \"awskms\" {\n  region     = \"us-east-1\"\n  kms_key_id = \"45e0519a-0d2f-424b-99cc-a9d0c70b5d74\"\n}"` |  |
| vault.server.standalone.enabled | bool | `true` |  |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.14.2](https://github.com/norwoodj/helm-docs/releases/v1.14.2)
