# OAEBUDT Dataspace Connector

This guide provides step-by-step instructions for deploying the OAEBUDT Dataspace Connector using Helm on a Kubernetes cluster. It is intended for administrators managing deployments for pilot participants.

## Prerequisites

Before deploying the dataspace connector, ensure the following tools are installed and properly configured:

- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html) (configured using `aws configure`)
- An AWS account with appropriate IAM permissions
- [kubectl](https://kubernetes.io/docs/tasks/tools/) (for accessing and managing EKS clusters)
- [Helm CLI](https://helm.sh/docs/intro/install/) (version 3.6 or higher; required for deploying Helm charts)

---

## Helm Deployment Overview

A Helm chart for the **OAEBUDT** connector is available at `connector/charts/oaebudt-connector`.  
This chart streamlines and standardizes the deployment process for different pilot participants on Kubernetes.

For each of the six pilot partners, a dedicated `values-<pilot-partner>.yaml` file is provided.  
This file contains the specific configuration required for each participant's connector deployment.

### Participant Namespace Mapping

| Participant        | Namespace ID     |
|--------------------|-----------------|
| jstor              | jstor           |
| liblynx            | liblynx         |
| michigan           | michigan        |
| punctumbooks       | punctumbooks    |
| ubiquitypress      | ubiquitypress   |
| knowledgeunlatched | knowledgeunlatched |

---

## API Endpoint

API endpoints are generated based on the global domain name specified in the corresponding `values-<pilot-partner>.yaml` file.  
Ensure this value is set correctly for each deployment to avoid endpoint conflicts.

---

## Deployment Steps

### 1. Prepare the Values File

- Locate or create the appropriate `values-<pilot-partner>.yaml` file under `connector/charts/oaebudt-connector/`.
- Edit this file to include all necessary configuration values for the selected participant.

### 2. Deploy the Connector

Run the following commands to deploy the connector for a specific participant:

```bash
cd connector/charts
helm install <release-name> ./oaebudt-connector \
  -n <participant-namespace> \
  -f ./oaebudt-connector/values-<pilot-partner>.yaml \
  --create-namespace
```

**Parameters:**
- `<release-name>`: A unique name for this Helm release (e.g., `knowledgeunlatched`)
- `<participant-namespace>`: The Kubernetes namespace for the participant (see table above)
- `<pilot-partner>`: The participant identifier (e.g., `knowledgeunlatched`)

**Example:** Deploying the Knowledge Unlatched connector:

```bash
cd connector/charts
helm install knowledgeunlatched ./oaebudt-connector \
  -n knowledgeunlatched \
  -f ./oaebudt-connector/values-knowledgeunlatched.yaml \
  --create-namespace
```

---

## Post-Deployment

- Verify the deployment status using `kubectl get pods -n <participant-namespace>`.
- Check logs for troubleshooting: `kubectl logs <pod-name> -n <participant-namespace>`.
- To upgrade or reconfigure, edit the values file and run `helm upgrade`.

---

## Additional Resources

- [Helm Documentation](https://helm.sh/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [OAEBUDT Project Repository](../)

---
