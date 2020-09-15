package io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime;

/**
 * DynamoManager is a based-implementation of JPA's EntityManager.
 * @author Brenno Fagundes <dev.whiteflag@gmail.com>
 */
public abstract class DynamoManager {
    /* Dynamo Manager should be able to do most of EntityManager's functionalities
     including: find, refresh, remove, persist, contains an entity, create a query.
     TODO: Do a EntityManager Implementation for Dynamo Manager
     */

    private final DynamoDatabase db = DynamoDatabase.instance();

    public void createNativeQuery() {
    }

    public void createNativePaginatedQuery() {}

    public <T> T find() {
        return null;
    }

    public <T> T merge() {
        return null;
    }

    public void persist() {}

    public void refresh() {}

    public void remove() {}
}
