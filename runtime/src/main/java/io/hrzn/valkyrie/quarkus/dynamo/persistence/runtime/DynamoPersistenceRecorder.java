package io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime;

import io.quarkus.runtime.annotations.Recorder;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Recorder
public class DynamoPersistenceRecorder {
    private List<String> entities = new ArrayList<>();

    public void enlistAllEntitiesFound(Set<String> entityClassNames) {
        entities.addAll(entityClassNames);
        Logger.getLogger("io.hrzn.valkyrie.dynamo.persistence")
                .debugf("List of entities found by Quarkus deployment:%n%s", entities);
    }
}
