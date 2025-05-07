package org.oaebudt.edc.core;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oaebudt.edc.core.store.MongoParticipantGroupStore;
import org.oaebudt.edc.spi.store.ParticipantGroup;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class MongoParticipantGroupStoreTest {

    static MongoDBContainer mongoContainer;
    static MongoClient client;
    MongoParticipantGroupStore store;

    @BeforeAll
    static void setupAll() {
        mongoContainer = new MongoDBContainer("mongo:6.0");
        mongoContainer.start();
        client = MongoClients.create(mongoContainer.getReplicaSetUrl());
    }

    @AfterAll
    static void tearDownAll() {
        client.close();
        mongoContainer.stop();
    }

    @BeforeEach
    void setup() {
        store = new MongoParticipantGroupStore(client);
    }

    @Test
    void testSaveAndFindById() {
        store.save("group1", "p1", "p2", "p3");

        ParticipantGroup group = store.findById("group1");
        assertNotNull(group);
        assertEquals("group1", group.id());
        assertEquals(Set.of("p1", "p2", "p3"), group.participants());
    }

    @Test
    void testUpdateExistingGroup() {
        store.save("group2", "a", "b");
        store.save("group2", "c", "d"); // should add to existing

        ParticipantGroup group = store.findById("group2");
        assertNotNull(group);
        assertEquals(Set.of("a", "b", "c", "d"), group.participants());
    }

    @Test
    void testGetAllGroups() {
        store.save("g1", "x");
        store.save("g2", "y");
        Set<ParticipantGroup> all = store.getAllGroups();
        assertEquals(2, all.size());
    }
}
