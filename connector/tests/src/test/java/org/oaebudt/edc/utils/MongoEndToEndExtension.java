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

package org.oaebudt.edc.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.eclipse.edc.spi.system.configuration.Config;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.MongoDBContainer;

import java.util.Map;

import static java.util.Map.entry;

public class MongoEndToEndExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String DEFAULT_IMAGE = "mongo:6.0";

    private final MongoDBContainer mongo;

    public MongoEndToEndExtension() {
        this(DEFAULT_IMAGE);
    }

    public MongoEndToEndExtension(String dockerImageName) {
        mongo = new MongoDBContainer(dockerImageName);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        mongo.stop();
        mongo.close();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        mongo.start();

        try (MongoClient client = MongoClients.create(mongo.getReplicaSetUrl())) {
            MongoDatabase db = client.getDatabase("oaebudt");
            db.createCollection("report");
        }
    }

    public Config config() {
        final var settings = Map.ofEntries(
                entry("web.datasource.mongo.url", mongo.getConnectionString())
        );

        return ConfigFactory.fromMap(settings);
    }
}
