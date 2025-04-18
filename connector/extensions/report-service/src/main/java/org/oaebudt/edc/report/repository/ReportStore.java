package org.oaebudt.edc.report.repository;

import org.oaebudt.edc.report.model.ReportType;

public interface ReportStore {
    void saveReport(String json, ReportType reportType);
}
