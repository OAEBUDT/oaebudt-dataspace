# oaebudt-dataspace-connector

# Helm Deployment Instructions

A Helm chart for the **OAEBUDT** connector has been developed at `connector/charts/oaebudt-connector`.  
This chart is designed to simplify and standardize the deployment of the OAEBUDT connector for different pilot participants on a Kubernetes cluster.

To deploy a Helm release for a specific participant connector from the six partners in the **OAEBUDT** pilot, a dedicated `values-<pilot-partner>.yaml` file has been created. This file is populated with the necessary configuration for the chosen participant connector.

## Release Name Convention

The release name should correspond to the specific participant in the pilot, chosen from the list of six partners. Ensure the release name aligns with the correct participant.

## Namespace Naming Convention

The namespace follows a specific format, detailed below:

**Namespace Format**:  
`participant-name ([PILOT PARTNER]) - namespace (participant-id)`

Here are the participant mappings for the namespaces:

| Participant        | Namespace ID     |
|--------------------|------------------|
| jstor              | participant-a    |
| liblynx            | participant-b    |
| michigan           | participant-c    |
| punctumbooks       | participant-d    |
| knowledgeunlatched | participant-e    |
| ubiquitypress      | participant-f    |

**Important**: The namespace must match the `participant-id` of the connector and should remain consistent. This is crucial because verifiable credentials are tied to the chart and referenced by the `participant.id`, as shown in the table above.

## API Endpoint

The API endpoints will be generated based on the global domain name defined in the `values-<pilot-partner>.yaml` file.

## Deployment Prerequisites

Before deploying, ensure the following prerequisites are met:

- A running Kubernetes cluster (version 1.22 or higher recommended).
- Helm installed (version 3.6 or higher).
- A correctly configured `values-<pilot-partner>.yaml` file located under `connector/charts/oaebudt-connector/` for the selected participant.

## Deployment Command

To deploy the connector for the selected participant, run the following command:

```bash
cd connector/charts
helm install <release-name> ./oaebudt-connector \
  -n <participant-id>  \
  -f ./oaebudt-connector/values-<release-name>.yaml \
  --create-namespace
```
Example for deploying the knowledgeunlatched connector:
```bash
cd connector/charts
helm install knowledgeunlatched ./oaebudt-connector \
  -n participant-e   \
  -f ./oaebudt-connector/values-knowledgeunlatched.yaml \
  --create-namespace
```
