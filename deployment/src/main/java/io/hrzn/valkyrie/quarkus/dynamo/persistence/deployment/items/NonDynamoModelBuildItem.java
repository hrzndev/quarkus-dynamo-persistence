package io.hrzn.valkyrie.quarkus.dynamo.persistence.deployment.items;

import io.quarkus.builder.item.MultiBuildItem;

public final class NonDynamoModelBuildItem extends MultiBuildItem {
    private final String className;

    public NonDynamoModelBuildItem(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
