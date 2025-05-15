package org.oaebudt.edc.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.junit.annotations.PostgresqlIntegrationTest;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.testfixtures.PostgresqlStoreSetupExtension;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.oaebudt.edc.core.store.SqlParticipantGroupStore;
import org.oaebudt.edc.spi.store.ParticipantGroup;


import static org.assertj.core.api.Assertions.assertThat;

@PostgresqlIntegrationTest
@ExtendWith(PostgresqlStoreSetupExtension.class)
public class SqlParticipantGroupStoreTest {

    private SqlParticipantGroupStore store;

    @BeforeEach
    void setUp(PostgresqlStoreSetupExtension extension, QueryExecutor queryExecutor) {
        var dataSourceRegistry = extension.getDataSourceRegistry();
        var datasourceName = extension.getDatasourceName();
        var dataSource = dataSourceRegistry.resolve(datasourceName);

        var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();

        this.store = new SqlParticipantGroupStore(
                dataSourceRegistry,
                datasourceName,
                extension.getTransactionContext(),
                new ObjectMapper(),
                queryExecutor
        );
    }

    @Test
    void save_thenFindById_shouldReturnGroupWithParticipants() {
        var groupId = "group-a";
        store.save(groupId, "p1", "p2");

        var result = store.findById(groupId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(groupId);
        assertThat(result.participants()).containsExactlyInAnyOrder("p1", "p2");
    }

    @Test
    void save_withNoParticipants_thenFindById_shouldReturnEmptyParticipants() {
        var groupId = "group-b";
        store.save(groupId); // no participants

        var result = store.findById(groupId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(groupId);
        assertThat(result.participants()).isEmpty();
    }

    @Test
    void getAllGroups_shouldReturnAllParticipantGroups() {
        var group1 = "group-x";
        var group2 = "group-y";
        store.save(group1, "x1", "x2");
        store.save(group2, "y1");

        var result = store.getAllGroups();

        assertThat(result)
                .extracting(ParticipantGroup::id)
                .contains(group1, group2);

        var g1 = result.stream().filter(g -> g.id().equals(group1)).findFirst().orElseThrow();
        var g2 = result.stream().filter(g -> g.id().equals(group2)).findFirst().orElseThrow();

        assertThat(g1.participants()).containsExactlyInAnyOrder("x1", "x2");
        assertThat(g2.participants()).containsExactly("y1");
    }

    @Test
    void save_whenGroupExists_shouldMergeParticipants() {
        var groupId = "group-merge";

        // First save
        store.save(groupId, "p1", "p2");

        // Second save with overlapping and new participants
        store.save(groupId, "p2", "p3", "p4");

        var result = store.findById(groupId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(groupId);
        assertThat(result.participants()).containsExactlyInAnyOrder("p1", "p2", "p3", "p4");
    }
}
