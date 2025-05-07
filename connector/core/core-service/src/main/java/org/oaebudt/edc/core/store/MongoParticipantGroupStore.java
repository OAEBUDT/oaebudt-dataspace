package org.oaebudt.edc.core.store;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.oaebudt.edc.spi.store.ParticipantGroup;
import org.oaebudt.edc.spi.store.ParticipantGroupStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.addEachToSet;

public class MongoParticipantGroupStore implements ParticipantGroupStore {

    public static final String DATABASE_NAME = "oaebudt";
    public static final String REPORT_COLLECTION = "participantGroup";
    private final MongoCollection<Document> collection;

    public MongoParticipantGroupStore(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        this.collection = database.getCollection(REPORT_COLLECTION);
    }

    @Override
    public ParticipantGroup findById(String groupId) {
        Document doc = collection.find(eq("_id", groupId)).first();
        if (doc == null) return null;

        List<String> participants = doc.getList("participants", String.class);
        return new ParticipantGroup(groupId, new HashSet<>(participants));
    }

    @Override
    public void save(String groupId, String... participantIds) {
        List<String> participantsSet = Arrays.asList(participantIds);
        Document existing = collection.find(eq("_id", groupId)).first();

        if (existing != null) {
            // Update participants list by adding new unique values
            collection.updateOne(
                    eq("_id", groupId),
                    addEachToSet("participants", participantsSet)
            );
        } else {
            // Create new group
            Document doc = new Document("_id", groupId)
                    .append("participants", new ArrayList<>(participantsSet));
            collection.insertOne(doc);
        }
    }

    @Override
    public Set<ParticipantGroup> getAllGroups() {
        Set<ParticipantGroup> groups = new HashSet<>();
        for (Document doc : collection.find()) {
            String id = doc.getString("_id");
            List<String> participants = doc.getList("participants", String.class);
            groups.add(new ParticipantGroup(id, new HashSet<>(participants)));
        }
        return groups;
    }
}
