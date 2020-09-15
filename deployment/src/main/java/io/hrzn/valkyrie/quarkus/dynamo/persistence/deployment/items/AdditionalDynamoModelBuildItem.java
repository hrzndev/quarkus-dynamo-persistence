package io.hrzn.valkyrie.quarkus.dynamo.persistence.deployment.items;

import io.quarkus.builder.item.MultiBuildItem;

public final class AdditionalDynamoModelBuildItem extends MultiBuildItem {
    private final String className;

    public AdditionalDynamoModelBuildItem(Class<?> clazz) {
        this.className = clazz.getName();
    }

    public String getClassName() {
        return className;
    }
}
