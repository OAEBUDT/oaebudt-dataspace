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

package org.oaebudt.edc.core.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.output.MigrateResult;

import javax.sql.DataSource;


public class FlywayManager {

    private static final String MIGRATION_LOCATION_BASE = "classpath:db/migration";

    public static MigrateResult migrate(DataSource dataSource, String defaultSchema, MigrationVersion target) {
        var flyway =
                Flyway.configure()
                        .baselineVersion(MigrationVersion.fromVersion("0.0.0"))
                        .failOnMissingLocations(true)
                        .dataSource(dataSource)
                        .locations(MIGRATION_LOCATION_BASE)
                        .defaultSchema(defaultSchema)
                        .target(target)
                        .load();

        flyway.baseline();

        return flyway.migrate();
    }
}
