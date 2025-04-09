package org.oaebudt.edc.dcp.policy;

/**
 * Defines standard EDC policy scopes.
 */
public interface PolicyScopes {
    String CATALOG_REQUEST_SCOPE = "request.catalog";
    String NEGOTIATION_REQUEST_SCOPE = "request.contract.negotiation";
    String TRANSFER_PROCESS_REQUEST_SCOPE = "request.transfer.process";

    String CATALOG_SCOPE = "catalog";
    String NEGOTIATION_SCOPE = "contract.negotiation";
    String TRANSFER_PROCESS_SCOPE = "transfer.process";
}
