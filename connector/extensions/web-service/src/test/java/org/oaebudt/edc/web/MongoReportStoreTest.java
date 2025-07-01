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

package org.oaebudt.edc.web;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oaebudt.edc.web.model.ReportType;
import org.oaebudt.edc.web.repository.ReportStore;
import org.oaebudt.edc.web.repository.MongoReportStoreImpl;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class MongoReportStoreTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    private static MongoClient mongoClient;
    private static ReportStore repository;

    @BeforeAll
    static void setUp() {
        mongoDBContainer.start();
        mongoClient = MongoClients.create(mongoDBContainer.getConnectionString());
        repository = new MongoReportStoreImpl(mongoClient);
    }

    @AfterAll
    static void tearDown() {
        mongoClient.close();
        mongoDBContainer.stop();
    }

    @Test
    void testSaveReport() {
        String json = """
                {
                  "title": "Title report",
                  "data": { "value": 100 }
                }
                """;

        repository.saveReport(json, ReportType.ITEM_REPORT);

        MongoDatabase database = mongoClient.getDatabase(MongoReportStoreImpl.DATABASE_NAME);
        MongoCollection<Document> collection = database.getCollection(MongoReportStoreImpl.REPORT_COLLECTION);
        Document savedDoc = collection.find(new Document("reportType", ReportType.ITEM_REPORT.name())).first();

        assert savedDoc != null;
        Assertions.assertEquals("Title report", savedDoc.getString("title"));
        Assertions.assertEquals(ReportType.ITEM_REPORT.name(), savedDoc.getString("reportType"));
    }
}
