package io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime.boot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * A class descriptor used to represent classes related to Dynamo Persistence.
 * @author Brenno Fagundes <dev.whiteflag@gmail.com>
 */
public class DynamoClassDescriptor {
    private String name;

    public DynamoClassDescriptor() {}

    public DynamoClassDescriptor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InputStream getStreamAccess() throws IOException {
        return Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource(name.replace('.', '/') + ".class")).openStream();
    }
}
