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
import java.util.List;

public class PhaseExecutionInfoTests extends AbstractSerializingTestCase<PhaseExecutionInfo> {

    static PhaseExecutionInfo randomPhaseExecutionInfo(String phaseName) {
        return new PhaseExecutionInfo(randomAlphaOfLength(5), PhaseTests.randomTestPhase(phaseName),
            randomNonNegativeLong(), randomNonNegativeLong());
    }

    String phaseName;

    @Before
    public void setupPhaseName() {
        phaseName = randomAlphaOfLength(7);
    }

    @Override
    protected PhaseExecutionInfo createTestInstance() {
        return randomPhaseExecutionInfo(phaseName);
    }

    @Override
    protected Reader<PhaseExecutionInfo> instanceReader() {
        return PhaseExecutionInfo::new;
    }

    @Override
    protected PhaseExecutionInfo doParseInstance(XContentParser parser) throws IOException {
        return PhaseExecutionInfo.parse(parser, phaseName);
    }

    @Override
    protected PhaseExecutionInfo mutateInstance(PhaseExecutionInfo instance) throws IOException {
        String policyName = instance.getPolicyName();
        Phase phase = instance.getPhase();
        long version = instance.getVersion();
        long modifiedDate = instance.getModifiedDate();
        switch (between(0, 3)) {
            case 0:
                policyName = policyName + randomAlphaOfLengthBetween(1, 5);
                break;
            case 1:
                phase = randomValueOtherThan(phase, () -> PhaseTests.randomTestPhase(randomAlphaOfLength(6)));
                break;
            case 2:
                version++;
                break;
            case 3:
                modifiedDate++;
                break;
            default:
                throw new AssertionError("Illegal randomisation branch");
        }
        return new PhaseExecutionInfo(policyName, phase, version, modifiedDate);
    }

    protected NamedWriteableRegistry getNamedWriteableRegistry() {
        return new NamedWriteableRegistry(Arrays
            .asList(new NamedWriteableRegistry.Entry(LifecycleAction.class, MockAction.NAME, MockAction::new)));
    }

    @Override
    protected NamedXContentRegistry xContentRegistry() {
        List<NamedXContentRegistry.Entry> entries = new ArrayList<>(ClusterModule.getNamedXWriteables());
        entries.add(new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(MockAction.NAME), MockAction::parse));
        return new NamedXContentRegistry(entries);
    }
}
