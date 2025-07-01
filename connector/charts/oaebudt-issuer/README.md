# oaebudt-issuer

This `values.yaml` file contains the necessary configuration to deploy an NGINX server (using the Bitnami NGINX chart) that hosts the DID document for **oaebudt-issuer**.

The DID document serves as a trusted source for validating the signatures of verifiable credentials within the data space.

## Important

> **Namespace Requirement**:  
> The namespace must be set to **`oaebudt`** and must not be changed.  
> It is directly referenced in the DID documents used by the connectors and is essential for ensuring that the issuer functions correctly within the Kubernetes cluster domain.  
> The DID documents are designed for **internal cluster use only**.

## Prerequisites

- [Helm](https://helm.sh/) installed
- Access to a Kubernetes cluster

## Installation

To deploy the issuer, execute the following command:

```bash
cd connector/charts/
helm install operas-issuer bitnami/nginx \
  -n operas \
  -f ./oaebudt-issuer/values.yaml \
  --create-namespace
