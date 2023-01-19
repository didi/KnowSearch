/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.core.ilm;

import org.elasticsearch.cluster.ClusterModule;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.io.stream.Writeable.Reader;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.AbstractSerializingTestCase;
import org.junit.Before;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LifecyclePolicyMetadataTests extends AbstractSerializingTestCase<LifecyclePolicyMetadata> {

    private String lifecycleName;

    @Before
    public void setup() {
        lifecycleName = randomAlphaOfLength(20);
    }

    @Override
    protected NamedWriteableRegistry getNamedWriteableRegistry() {
        return new NamedWriteableRegistry(
            Arrays.asList(
                new NamedWriteableRegistry.Entry(LifecycleAction.class, MockAction.NAME, MockAction::new),
                new NamedWriteableRegistry.Entry(LifecycleType.class, TimeseriesLifecycleType.TYPE,
                    (in) -> TimeseriesLifecycleType.INSTANCE),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, AllocateAction.NAME, AllocateAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, WaitForSnapshotAction.NAME, WaitForSnapshotAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, DeleteAction.NAME, DeleteAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, ForceMergeAction.NAME, ForceMergeAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, ReadOnlyAction.NAME, ReadOnlyAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, RolloverAction.NAME, RolloverAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, ShrinkAction.NAME, ShrinkAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, FreezeAction.NAME, FreezeAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, SetPriorityAction.NAME, SetPriorityAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, UnfollowAction.NAME, UnfollowAction::new)
            ));
    }

    @Override
    protected NamedXContentRegistry xContentRegistry() {
        List<NamedXContentRegistry.Entry> entries = new ArrayList<>(ClusterModule.getNamedXWriteables());
        entries.addAll(Arrays.asList(
            new NamedXContentRegistry.Entry(LifecycleType.class, new ParseField(TimeseriesLifecycleType.TYPE),
                (p) -> TimeseriesLifecycleType.INSTANCE),
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(AllocateAction.NAME), AllocateAction::parse),
            new NamedXContentRegistry.Entry(LifecycleAction.class,
                new ParseField(WaitForSnapshotAction.NAME), WaitForSnapshotAction::parse),
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(DeleteAction.NAME), DeleteAction::parse),
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(ForceMergeAction.NAME), ForceMergeAction::parse),
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(ReadOnlyAction.NAME), ReadOnlyAction::parse),
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(RolloverAction.NAME), RolloverAction::parse),
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(ShrinkAction.NAME), ShrinkAction::parse),
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(FreezeAction.NAME), FreezeAction::parse),
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(SetPriorityAction.NAME), SetPriorityAction::parse),
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(UnfollowAction.NAME), UnfollowAction::parse)
        ));
        return new NamedXContentRegistry(entries);
    }

    @Override
    protected LifecyclePolicyMetadata doParseInstance(XContentParser parser) throws IOException {
        return LifecyclePolicyMetadata.parse(parser, lifecycleName);
    }

    @Override
    protected LifecyclePolicyMetadata createTestInstance() {
        Map<String, String> headers = new HashMap<>();
        int numberHeaders = between(0, 10);
        for (int i = 0; i < numberHeaders; i++) {
            headers.put(randomAlphaOfLength(10), randomAlphaOfLength(10));
        }
        return new LifecyclePolicyMetadata(LifecyclePolicyTests.randomTimeseriesLifecyclePolicy(lifecycleName), headers,
            randomNonNegativeLong(), randomNonNegativeLong());
    }

    @Override
    protected Reader<LifecyclePolicyMetadata> instanceReader() {
        return LifecyclePolicyMetadata::new;
    }

    @Override
    protected LifecyclePolicyMetadata mutateInstance(LifecyclePolicyMetadata instance) throws IOException {
        LifecyclePolicy policy = instance.getPolicy();
        Map<String, String> headers = instance.getHeaders();
        long version = instance.getVersion();
        long creationDate = instance.getModifiedDate();
        switch (between(0, 3)) {
        case 0:
            policy = new LifecyclePolicy(TimeseriesLifecycleType.INSTANCE, policy.getName() + randomAlphaOfLengthBetween(1, 5),
                    policy.getPhases());
            break;
        case 1:
            headers = new HashMap<>(headers);
            headers.put(randomAlphaOfLength(11), randomAlphaOfLength(11));
            break;
        case 2:
            version++;
            break;
        case 3:
            creationDate++;
            break;
        default:
            throw new AssertionError("Illegal randomisation branch");
        }
        return new LifecyclePolicyMetadata(policy, headers, version, creationDate);
    }

}
