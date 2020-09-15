package io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime.utils;

import io.hrzn.valkyrie.quarkus.dynamodb.persistence.annotations.DynamoEntity;
import io.hrzn.valkyrie.quarkus.dynamodb.persistence.annotations.DynamoID;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import javax.enterprise.context.ApplicationScoped;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * @author Whiteflag <dev.whiteflag@gmail.com>
 */
@ApplicationScoped
@RegisterForReflection
public class DynamoClassUtils {
    public Collection<Field> getEntityDeclaredFields(Class<?> entity) {
        Collection<Field> attributes = new ArrayList<>();
        Arrays.stream(entity.getDeclaredFields()).parallel().forEach(field -> {
            if (field == null)
                return;
            attributes.add(field);
        });
        return attributes;
    }

    public Collection<Class<? extends Number>> getClassesExtendsNumber() {
        Reflections reflections = new Reflections("");
        return reflections.getSubTypesOf(Number.class);
    }

    public Class<?> getClassesByName(String name) {
        Set<Class<?>> classes = getClassesWithDynamoEntity();
        Collection<Class<?>> foundClasses = new ArrayList<>();

        classes.parallelStream().forEach(aClass -> {
            if (aClass.getSimpleName().equals(name))
                foundClasses.add(aClass);
        });
        return (Class<?>) foundClasses.toArray()[0];
    }

    public Set<Class<?>> getClassesWithDynamoEntity() {
        // TODO: Reflections is looking in EVERYTHING for the Entity. Needs Optimization.
        Reflections reflections = new Reflections("");
        return reflections.getTypesAnnotatedWith(DynamoEntity.class);
    }

    public Set<Field> getFieldsWithDynamoID(Class<?> tClass) {
        Reflections reflections = new Reflections(tClass, new FieldAnnotationsScanner());
        return reflections.getFieldsAnnotatedWith(DynamoID.class);
    }
}
