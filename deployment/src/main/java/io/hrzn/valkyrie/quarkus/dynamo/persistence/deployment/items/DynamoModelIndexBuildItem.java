package io.hrzn.valkyrie.quarkus.dynamo.persistence.deployment.items;

import io.quarkus.builder.item.SimpleBuildItem;
import org.jboss.jandex.CompositeIndex;

public final class DynamoModelIndexBuildItem extends SimpleBuildItem {
    private final CompositeIndex index;

    public DynamoModelIndexBuildItem(CompositeIndex index) {
        this.index = index;
    }

    public CompositeIndex getIndex() {
        return index;
    }
}
