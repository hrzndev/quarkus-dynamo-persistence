package io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime.boot;

import java.util.Set;

/**
 * A simple placeholder scanner for classDescriptors.
 * @author Whiteflag <dev.whiteflag@gmail.com>
 */
public class DynamoScanner {
    private Set<DynamoClassDescriptor> classDescriptors;

    public Set<DynamoClassDescriptor> getClassDescriptors() {
        return classDescriptors;
    }

    public void setClassDescriptors(Set<DynamoClassDescriptor> classDescriptors) {
        this.classDescriptors = classDescriptors;
    }
}
