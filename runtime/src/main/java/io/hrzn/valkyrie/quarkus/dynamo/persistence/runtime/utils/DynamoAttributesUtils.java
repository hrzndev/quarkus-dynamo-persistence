package io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime.utils;

import io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime.exceptions.DynamoIntegrationException;
import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * @author Whiteflag <dev.whiteflag@gmail.com>
 */
@Singleton
@RegisterForReflection
public class DynamoAttributesUtils {

    @Inject DynamoClassUtils ClassUtils;

    public DynamoAttributesUtils() {}

    private static class DynamoAttributesUtilsHolder {
        public static final DynamoAttributesUtils instance = new DynamoAttributesUtils();
    }

    public static DynamoAttributesUtils instance() {
        return DynamoAttributesUtilsHolder.instance;
    }

    public Collection<AttributeDefinition> attributeDefinitionsFromEntity(Class<?> entity) {
        Collection<AttributeDefinition> definedAttributes = new ArrayList<>();

        ClassUtils.getEntityDeclaredFields(entity).parallelStream().forEach(field -> {
            definedAttributes.add(AttributeDefinition.builder().attributeName(field.getName())
                    .attributeType(getScalarAttributeType(field.getType())).build());
        });

        return definedAttributes;
    }

    private ScalarAttributeType getScalarAttributeType(Type type) {
        if (getIfTypeIsNumber(type.getTypeName()))
            return ScalarAttributeType.N;
        if (getIfTypeIsNumber(type))
            return ScalarAttributeType.N;
        if (getIfTypeIsString(type.getTypeName()))
            return ScalarAttributeType.S;
        throw new DynamoIntegrationException("Unidentified type: " + type.getTypeName());
    }

    private boolean getIfTypeIsNumber(String typeName){
        String[] types = {"int", "float", "double", "short", "byte"};
        return Arrays.stream(types).anyMatch(Predicate.isEqual(typeName));
    }

    private boolean getIfTypeIsNumber(Type type){
        Collection<String> classes = new ArrayList<>();
        ClassUtils.getClassesExtendsNumber().parallelStream().forEach(aClass -> {
            if (aClass.getTypeName().equals(type.getTypeName()))
                classes.add(aClass.getTypeName());
        });
        return !classes.isEmpty();
    }

    private boolean getIfTypeIsString(String typeName){
        String[] types = {"String", "ZonedDateTime", "boolean", "char", "byte"};
        return Arrays.stream(types).anyMatch(Predicate.isEqual(typeName));
    }
}
