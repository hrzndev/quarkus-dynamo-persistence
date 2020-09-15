package io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime;

import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Singleton;

/**
 * DynamoDatabase is a class responsible to abstract the {@link DynamoDbClient} client.
 * @author Brenno Fagundes <dev.whiteflag@gmail.com>
 */
@Singleton
@RegisterForReflection
public class DynamoDatabase {
    private final DynamoDbClient client;

    protected DynamoDatabase() {
        this.client = DynamoDbClient.create();
    }

    protected DynamoDatabase(DynamoDbClient client) {
        this.client = client;
    }

    private static class DynamoDatabaseHolder {
        public static final DynamoDatabase instance = new DynamoDatabase();
    }

    public static DynamoDatabase instance() {
        return DynamoDatabaseHolder.instance;
    }
}
