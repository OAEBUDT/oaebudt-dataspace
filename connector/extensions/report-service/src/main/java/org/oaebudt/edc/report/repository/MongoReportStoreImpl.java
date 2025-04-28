package org.oaebudt.edc.report.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.oaebudt.edc.report.model.ReportType;

import java.util.Objects;

public class MongoReportStoreImpl implements ReportStore {
    public static final String DATABASE_NAME = "oaebudt";
    public static final String REPORT_COLLECTION = "report";
    private final MongoDatabase database;

    public MongoReportStoreImpl(MongoClient mongoClient) {
        database = mongoClient.getDatabase(DATABASE_NAME);
    }

    @Override
    public void saveReport(String json, ReportType reportType) {
        MongoCollection<Document> collection = database.getCollection(REPORT_COLLECTION);

        Document doc = Document.parse(json);
        doc.put("reportType", reportType.name());

        // Replace the existing document with the same reportType or insert if not found
        collection.replaceOne(
                Filters.eq("reportType", reportType.name()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public Document getReportByType(ReportType reportType) {
        MongoCollection<Document> collection = database.getCollection(REPORT_COLLECTION);

        return collection.find(Filters.eq("reportType", reportType.name())).first();
    }
}
