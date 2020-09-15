package io.hrzn.valkyrie.quarkus.dynamo.persistence.deployment;

import io.hrzn.valkyrie.quarkus.dynamo.persistence.deployment.items.DynamoEntitiesBuildItem;
import io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime.annotations.DynamoEntity;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.configuration.ConfigurationError;
import org.jboss.jandex.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// https://github.com/quarkusio/quarkus/blob/c50a54b83a5fc7ae9d5683465372a45df51590d2/extensions/
// hibernate-orm/deployment/src/main/java/io/quarkus/hibernate/orm/deployment/JpaJandexScavenger.java#L73
final class DynamoJandexScavenger {
    private static final DotName DYNAMO_ENTITY = DotName.createSimple(DynamoEntity.class.getName());
    private static final DotName ENUM = DotName.createSimple(Enum.class.getName());

    private final BuildProducer<ReflectiveClassBuildItem> reflectiveClass;
    private final IndexView indexView;
    private final Set<String> nonDynamoModelClasses;
    private final Set<String> ignorableNonIndexedClasses;

    public DynamoJandexScavenger(BuildProducer<ReflectiveClassBuildItem> reflectiveClass, IndexView indexView,
                                 Set<String> nonDynamoModelClasses, Set<String> ignorableNonIndexedClasses) {
        this.reflectiveClass = reflectiveClass;
        this.indexView = indexView;
        this.nonDynamoModelClasses = nonDynamoModelClasses;
        this.ignorableNonIndexedClasses = ignorableNonIndexedClasses;
    }

    public DynamoEntitiesBuildItem discoverModelAndRegisterForReflection() {
        final DynamoEntitiesBuildItem domainObjectCollector = new DynamoEntitiesBuildItem();
        final Set<String> javaTypeCollector = new HashSet<>();
        final Set<String> enumTypeCollector = new HashSet<>();
        final Set<DotName> unindexedClasses = new HashSet<>();

        enlistDynamoModelClasses(indexView, domainObjectCollector, enumTypeCollector, javaTypeCollector,
                DYNAMO_ENTITY, unindexedClasses);

        domainObjectCollector.registerAllForReflection(reflectiveClass);

        if (!enumTypeCollector.isEmpty()) {
            reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, Enum.class.getName()));
            enumTypeCollector.parallelStream().forEach(className ->
                    reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, className)));
        }

        javaTypeCollector.parallelStream().forEach(javaType ->
                reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, javaType)));

        if (!unindexedClasses.isEmpty()) {
            Set<String> unIgnorableIndexedClasses = unindexedClasses.stream().map(DotName::toString)
                    .collect(Collectors.toSet());
            unIgnorableIndexedClasses.removeAll(ignorableNonIndexedClasses);

            if (!unIgnorableIndexedClasses.isEmpty()) {
                final String unIgnorableNotIndexedClasses = unIgnorableIndexedClasses.stream()
                        .map(d -> "\t- " + d + "\n").collect(Collectors.joining());
                throw new ConfigurationError(
                        "Unable to properly register the hierarchy of the following Dynamo classes as they" +
                                " are not in the Jandex index:\n" + unIgnorableNotIndexedClasses
                                + "Consider adding them to the index either by creating a Jandex index " +
                                "for your dependency via the Maven plugin, quarkus.index-dependency properties.");
            }
        }

        return domainObjectCollector;
    }

    private void enlistDynamoModelClasses(IndexView index, DynamoEntitiesBuildItem domainObjectCollector,
                                          Set<String> enumTypeCollector, Set<String> javaTypeCollector,
                                          DotName dotName, Set<DotName> unindexedClasses) {
        Collection<AnnotationInstance> dynamoAnnotations = index.getAnnotations(dotName);

        if (dynamoAnnotations == null)
            return;

        dynamoAnnotations.parallelStream().forEach(annotation -> {
            ClassInfo clazz = annotation.target().asClass();
            DotName targetDotName = clazz.name();

            if (nonDynamoModelClasses.contains(targetDotName.toString()))
                return;

            addClassHierarchyToReflectiveList(index, domainObjectCollector, enumTypeCollector,
                    javaTypeCollector, targetDotName, unindexedClasses);
            collectDomainObject(domainObjectCollector, clazz);
        });
    }

    private static void addClassHierarchyToReflectiveList(IndexView index, DynamoEntitiesBuildItem domainObjectCollector,
                                                          Set<String> enumTypeCollector, Set<String> javaTypeCollector,
                                                          DotName className, Set<DotName> unindexedClasses) {
        if (className == null || isClassIgnored(className))
            return;

        if (isInJavaPackage(className)) {
            javaTypeCollector.add(className.toString());
            return;
        }

        ClassInfo classInfo = index.getClassByName(className);
        if (classInfo == null) {
            unindexedClasses.add(className);
            return;
        }

        for (FieldInfo fieldInfo : classInfo.fields()) {
            DotName fieldType = fieldInfo.type().name();
            ClassInfo fieldTypeClassInfo = index.getClassByName(fieldType);
            if (fieldTypeClassInfo != null && ENUM.equals(fieldTypeClassInfo.superName())) {
                enumTypeCollector.add(fieldType.toString());
            }
        }

        collectDomainObject(domainObjectCollector, classInfo);

        addClassHierarchyToReflectiveList(index, domainObjectCollector, enumTypeCollector,
                javaTypeCollector, classInfo.superName(), unindexedClasses);

        for (DotName interfaceDotName : classInfo.interfaceNames()) {
            addClassHierarchyToReflectiveList(index, domainObjectCollector, enumTypeCollector,
                    javaTypeCollector, interfaceDotName, unindexedClasses);
        }
    }

    private static void collectDomainObject(DynamoEntitiesBuildItem domainObjectCollector, ClassInfo modelClass) {
        if (modelClass.classAnnotation(DYNAMO_ENTITY) != null)
            domainObjectCollector.addEntityClass(modelClass.name().toString());
        else
            domainObjectCollector.addModelClass(modelClass.name().toString());
    }

    private static boolean isClassIgnored(DotName classDotName) {
        String className = classDotName.toString();
        return className.startsWith("java.util.") || className.startsWith("java.lang.");
    }

    private static boolean isInJavaPackage(DotName classDotName) {
        String className = classDotName.toString();
        return className.startsWith("java.");
    }
}
