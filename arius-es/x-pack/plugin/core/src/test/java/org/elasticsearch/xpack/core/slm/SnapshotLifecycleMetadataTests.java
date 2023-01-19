/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.core.slm;

import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.AbstractSerializingTestCase;
import org.elasticsearch.xpack.core.ilm.OperationMode;
import org.elasticsearch.xpack.slm.SnapshotLifecycleStatsTests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SnapshotLifecycleMetadataTests extends AbstractSerializingTestCase<SnapshotLifecycleMetadata> {
    @Override
    protected SnapshotLifecycleMetadata doParseInstance(XContentParser parser) throws IOException {
        return SnapshotLifecycleMetadata.PARSER.apply(parser, null);
    }

    @Override
    protected SnapshotLifecycleMetadata createTestInstance() {
        int policyCount = randomIntBetween(0, 3);
        Map<String, SnapshotLifecyclePolicyMetadata> policies = new HashMap<>(policyCount);
        for (int i = 0; i < policyCount; i++) {
            String id = "policy-" + randomAlphaOfLength(3);
            policies.put(id, SnapshotLifecyclePolicyMetadataTests.createRandomPolicyMetadata(id));
        }
        return new SnapshotLifecycleMetadata(policies, randomFrom(OperationMode.values()),
            SnapshotLifecycleStatsTests.randomLifecycleStats());
    }

    @Override
    protected Writeable.Reader<SnapshotLifecycleMetadata> instanceReader() {
        return SnapshotLifecycleMetadata::new;
    }
}
