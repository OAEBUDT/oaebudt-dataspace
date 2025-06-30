/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.oaebudt.edc.web.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.oaebudt.edc.web.model.ReportType;

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
