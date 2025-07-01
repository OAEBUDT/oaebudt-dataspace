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

package org.oaebudt.edc.core.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.statement.SqlStatements;
import org.eclipse.edc.sql.store.AbstractSqlStore;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.oaebudt.edc.spi.store.ParticipantGroup;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SqlParticipantGroupStore extends AbstractSqlStore implements ParticipantGroupStore, SqlStatements {

    public static final String PARTICIPANT_GROUP_TABLE = "participant_group";
    public static final String ID_COLUMN = "id";
    public static final String PARTICIPANTS_COLUMN = "participants";

    private final ObjectMapper mapper;

    public SqlParticipantGroupStore(DataSourceRegistry dataSourceRegistry,
                                    String dataSourceName,
                                    TransactionContext transactionContext,
                                    ObjectMapper objectMapper,
                                    QueryExecutor queryExecutor) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper, queryExecutor);
        this.mapper = objectMapper;
    }

    @Override
    public ParticipantGroup findById(String groupId) {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                return queryExecutor.single(connection, true, this::mapParticipantGroup, getFindByIdQuery(), groupId);
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public void save(String groupId, String... participantIds) {
        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                // Get existing participants
                Set<String> existing = Optional.ofNullable(findById(groupId))
                        .map(ParticipantGroup::participants)
                        .orElse(new HashSet<>());

                // Merge with new ones
                existing.addAll(Arrays.asList(participantIds));

                String jsonbArray = mapper.writeValueAsString(existing);

                // Upsert group with new JSONB array
                queryExecutor.execute(connection, getUpsertGroupWithParticipantsQuery(), groupId, jsonbArray);
            } catch (Exception e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public Set<ParticipantGroup> getAllGroups() {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                return queryExecutor.query(connection, true, this::mapParticipantGroup, getFindAllQuery())
                        .collect(Collectors.toSet());
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    private String getFindByIdQuery() {
        return "SELECT id, participants FROM %s WHERE id = ?".formatted(PARTICIPANT_GROUP_TABLE);
    }

    private String getFindAllQuery() {
        return "SELECT id, participants FROM %s".formatted(PARTICIPANT_GROUP_TABLE);
    }

    private String getUpsertGroupWithParticipantsQuery() {
        return """
                INSERT INTO %s (id, participants)
                VALUES (?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET participants = EXCLUDED.participants
                """.formatted(PARTICIPANT_GROUP_TABLE);
    }

    private ParticipantGroup mapParticipantGroup(ResultSet resultSet) throws SQLException {
        var id = resultSet.getString(ID_COLUMN);
        var jsonb = resultSet.getString(PARTICIPANTS_COLUMN);

        Set<String> participants;
        try {
            String[] arr = mapper.readValue(jsonb, String[].class);
            participants = arr == null ? new HashSet<>() : new HashSet<>(Arrays.asList(arr));
        } catch (Exception e) {
            throw new SQLException("Failed to parse JSONB participants", e);
        }

        return new ParticipantGroup(id, participants);
    }
}
