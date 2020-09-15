package io.hrzn.valkyrie.quarkus.dynamo.persistence.deployment.items;

import io.quarkus.builder.item.MultiBuildItem;

import java.util.Set;

public final class IgnorableNonIndexedClasses extends MultiBuildItem {
    private final Set<String> classes;

    public IgnorableNonIndexedClasses(Set<String> classes) {
        this.classes = classes;
    }

    public Set<String> getClasses() {
        return classes;
    }
}
