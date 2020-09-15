package io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime.exceptions;

/**
 * @author Whiteflag <dev.whiteflag@gmail.com>
 */
public class DynamoIntegrationException extends RuntimeException {
    public DynamoIntegrationException() {}

    public DynamoIntegrationException(String message) {
        super(message);
    }
}
