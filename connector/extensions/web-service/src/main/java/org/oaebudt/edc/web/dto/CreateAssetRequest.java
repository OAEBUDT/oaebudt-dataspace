package org.oaebudt.edc.web.dto;

import org.oaebudt.edc.web.model.ReportType;

import java.util.Map;

public record CreateAssetRequest(
        String title,
        Map<String, Object> metadata,
        ReportType reportType,
        String accessDefinition,
        String url,
        String method,
        String authHeaderKey,
        String authCode,
        Map<String, Object> headers
) {}
