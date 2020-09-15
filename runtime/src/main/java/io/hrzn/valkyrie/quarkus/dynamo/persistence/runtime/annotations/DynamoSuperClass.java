package io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamoSuperClass {
}
