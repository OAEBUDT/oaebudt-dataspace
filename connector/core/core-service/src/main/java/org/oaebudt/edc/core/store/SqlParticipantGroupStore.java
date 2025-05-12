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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SqlParticipantGroupStore extends AbstractSqlStore implements ParticipantGroupStore, SqlStatements {


    public static final String PARTICIPANT_GROUP_TABLE = "participant_group";
    public static final String PARTICIPANT_TABLE = "participant";
    public static final String ID_COLUMN = "id";
    public static final String GROUP_ID_COLUMN = "group_id";
    public static final String PARTICIPANT_ID_COLUMN = "participant_id";

    public SqlParticipantGroupStore(DataSourceRegistry dataSourceRegistry,
                                    String dataSourceName,
                                    TransactionContext transactionContext,
                                    ObjectMapper objectMapper,
                                    QueryExecutor queryExecutor) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper, queryExecutor);
    }

    @Override
    public ParticipantGroup findById(String groupId) {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                Set<String> participantIds = queryExecutor.query(connection, false, this::mapParticipant, this.findParticipantsByGroupId(), groupId)
                        .collect(Collectors.toSet());

                return new ParticipantGroup(groupId, participantIds);
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public void save(String groupId, String... participantIds) {
        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                queryExecutor.execute(connection, getUpsertParticipantGroupStatement(), groupId);

                if(participantIds.length > 0) {
                    queryExecutor.execute(connection, getUpsertMultipleParticipantStatement(groupId, participantIds));
                }
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public Set<ParticipantGroup> getAllGroups() {

        return transactionContext.execute(() -> {

            try (var connection = getConnection()) {

                Map<String, Set<String>> grouped =queryExecutor.query(connection, false, this::mapParticipantGroup, this.findAllParticipants())
                        .collect(Collectors.groupingBy(
                                arr -> arr[0], // key
                                Collectors.mapping(
                                        arr -> arr[1], // value
                                        Collectors.filtering(
                                                Objects::nonNull,
                                                Collectors.toSet()
                                        )
                                )
                        ));

                Set<ParticipantGroup> result = new HashSet<>();
                for (Map.Entry<String, Set<String>> entry : grouped.entrySet()) {
                    result.add(new ParticipantGroup(entry.getKey(), entry.getValue()));
                }
                return result;
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }


    public String getUpsertParticipantGroupStatement() {
        return "INSERT INTO %s (%s) VALUES (?) ON CONFLICT DO NOTHING;".formatted(PARTICIPANT_GROUP_TABLE, "id");
    }

    public String findParticipantsByGroupId() {
        return "SELECT %s FROM %s WHERE %s = ?".formatted(PARTICIPANT_ID_COLUMN, PARTICIPANT_TABLE, GROUP_ID_COLUMN);
    }

    public String findAllParticipants() {
        return "SELECT pg.id, participant_id " +
                "FROM participant_group pg " +
                "LEFT JOIN participant p ON " +
                "pg.id = p.group_id";
    }

    public String getUpsertMultipleParticipantStatement(String groupId, String... participantIds) {
        var str = Arrays.stream(participantIds).map(participantId -> "('%s', '%s')".formatted(groupId, participantId)).collect(Collectors.joining(","));
        return "INSERT INTO %s (%s, %s) VALUES %s ON CONFLICT DO NOTHING;".formatted(PARTICIPANT_TABLE, GROUP_ID_COLUMN, PARTICIPANT_ID_COLUMN, str);
    }

    private String mapParticipant(ResultSet resultSet) throws SQLException {
        return resultSet.getString(PARTICIPANT_ID_COLUMN);
    }

    private String[] mapParticipantGroup(ResultSet resultSet) throws SQLException {
        var participantId =  resultSet.getString(PARTICIPANT_ID_COLUMN);
        var groupId = resultSet.getString(ID_COLUMN);

        return new String[]{groupId, participantId};
    }
}
