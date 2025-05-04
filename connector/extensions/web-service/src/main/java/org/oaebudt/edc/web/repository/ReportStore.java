package org.oaebudt.edc.web.repository;

import org.bson.Document;
import org.oaebudt.edc.web.model.ReportType;

public interface ReportStore {
    void saveReport(String json, ReportType reportType);

    Document getReportByType(ReportType reportType);
}
