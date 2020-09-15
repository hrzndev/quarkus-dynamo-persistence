package io.hrzn.valkyrie.quarkus.dynamo.persistence.deployment;

import io.hrzn.valkyrie.quarkus.dynamo.persistence.runtime.DynamoPersistenceRecorder;
import io.hrzn.valkyrie.quarkus.dynamo.persistence.deployment.items.*;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CapabilityBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.index.IndexingUtil;
import io.quarkus.deployment.recording.RecorderContext;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Indexer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

public final class DynamoPersistenceProcessor {
    private static final Logger LOG = Logger.getLogger(DynamoPersistenceProcessor.class.getSimpleName());

    @BuildStep
    @Record(STATIC_INIT)
    public void build(RecorderContext recorderContext, DynamoPersistenceRecorder recorder,
                      DynamoEntitiesBuildItem domainObjects,
                      List<NonDynamoModelBuildItem> nonDynamoModelBuildItems,
                      BuildProducer<FeatureBuildItem> feature,
                      BuildProducer<BeanContainerListenerBuildItem> beanContainerListener) throws Exception {
        final boolean enablePersistence = hasEntities(domainObjects, nonDynamoModelBuildItems);
        feature.produce(new FeatureBuildItem("DYNAMO_PERSISTENCE"));

        if (!enablePersistence)
            return;

        recorder.enlistAllEntitiesFound(domainObjects.getEntityClassNames());

        // TODO: Fix this listener that doesn't have anything right now.
        beanContainerListener.produce(new BeanContainerListenerBuildItem(
                container -> {}
        ));
    }

    @BuildStep
    CapabilityBuildItem capability() {
        return new CapabilityBuildItem("AMAZON_DYNAMODB");
    }

    @BuildStep
    public DynamoModelIndexBuildItem indexDynamoEntities(
            CombinedIndexBuildItem index, List<AdditionalDynamoModelBuildItem> additionalDynamoBuildItems) {
        Indexer indexer = new Indexer();
        Set<DotName> additionalIndex = new HashSet<>();

        additionalDynamoBuildItems.parallelStream().forEach(model ->
                IndexingUtil.indexClass(model.getClassName(), indexer, index.getIndex(), additionalIndex,
                DynamoPersistenceProcessor.class.getClassLoader()));

        CompositeIndex compositeIndex = CompositeIndex.create(index.getIndex(), indexer.complete());
        return new DynamoModelIndexBuildItem(compositeIndex);
    }

    @BuildStep
    public void defineDynamoEntities(
            DynamoModelIndexBuildItem indexBuildItem,
            BuildProducer<DynamoEntitiesBuildItem> domainObjectsProducer,
            List<IgnorableNonIndexedClasses> ignorableNonIndexedClassesBuildItems,
            List<NonDynamoModelBuildItem> nonDynamoModelBuildItems,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass) throws Exception {
        Set<String> nonDynamoModelClasses = nonDynamoModelBuildItems.stream()
                .map(NonDynamoModelBuildItem::getClassName).collect(Collectors.toSet());

        Set<String> ignorableNonIndexedClasses = Collections.emptySet();
        if (!ignorableNonIndexedClassesBuildItems.isEmpty()) {
            ignorableNonIndexedClasses = new HashSet<>();
            for (IgnorableNonIndexedClasses buildItem : ignorableNonIndexedClassesBuildItems) {
                ignorableNonIndexedClasses.addAll(buildItem.getClasses());
            }
        }

        DynamoJandexScavenger scavenger = new DynamoJandexScavenger(reflectiveClass, indexBuildItem.getIndex(),
                nonDynamoModelClasses, ignorableNonIndexedClasses);
        final DynamoEntitiesBuildItem domainObjects = scavenger.discoverModelAndRegisterForReflection();
        domainObjectsProducer.produce(domainObjects);
    }

    private boolean hasEntities(DynamoEntitiesBuildItem dynamoEntities,
                                List<NonDynamoModelBuildItem> nonDynamoModels) {
        return !dynamoEntities.getEntityClassNames().isEmpty() || !nonDynamoModels.isEmpty();
    }
}
