package org.oaebudt.edc.report.dto;

import org.oaebudt.edc.report.model.ReportType;

import java.util.Map;

public class CreateAssetRequest {
    private String title;
    private String metadata;
    private ReportType reportType;
    private String accessDefinition;

    private String url;
    private String method;
    private String authHeaderKey;
    private String authCode;
    private Map<String, Object> headers;

    public CreateAssetRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public String getAccessDefinition() {
        return accessDefinition;
    }

    public void setAccessDefinition(String accessDefinition) {
        this.accessDefinition = accessDefinition;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAuthHeaderKey() {
        return authHeaderKey;
    }

    public void setAuthHeaderKey(String authHeaderKey) {
        this.authHeaderKey = authHeaderKey;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
