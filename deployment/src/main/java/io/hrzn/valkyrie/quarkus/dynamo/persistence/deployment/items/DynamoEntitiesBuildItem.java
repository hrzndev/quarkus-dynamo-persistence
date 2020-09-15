package io.hrzn.valkyrie.quarkus.dynamo.persistence.deployment.items;

import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

import java.util.HashSet;
import java.util.Set;

public final class DynamoEntitiesBuildItem extends SimpleBuildItem {
    private final Set<String> entityClassNames = new HashSet<>();
    private final Set<String> modelClassNames = new HashSet<>();

    public void addEntityClass(final String className) {
        entityClassNames.add(className);
        modelClassNames.add(className);
    }

    public void addModelClass(final String className) {
        modelClassNames.add(className);
    }

    public void registerAllForReflection(final BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        modelClassNames.parallelStream().forEach(className -> {
            reflectiveClass.produce(new ReflectiveClassBuildItem(true, true, className));
        });
    }

    /**
     * @return the list of entities (i.e. classes marked with DynamoEntity)
     */
    public Set<String> getEntityClassNames() {
        return entityClassNames;
    }

    /**
     * @return the list of all model class names: entities, mapped super classes...
     */
    public Set<String> getModelClassNames() {
        return modelClassNames;
    }
}
