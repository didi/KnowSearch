/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.client.indexlifecycle;

import org.elasticsearch.cluster.ClusterModule;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.AbstractXContentTestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.elasticsearch.client.indexlifecycle.LifecyclePolicyTests.createRandomPolicy;

public class GetLifecyclePolicyResponseTests extends AbstractXContentTestCase<GetLifecyclePolicyResponse> {

    @Override
    protected GetLifecyclePolicyResponse createTestInstance() {
        int numPolicies = randomIntBetween(1, 10);
        ImmutableOpenMap.Builder<String, LifecyclePolicyMetadata> policies = ImmutableOpenMap.builder();
        for (int i = 0; i < numPolicies; i++) {
            String policyName = "policy-" + randomAlphaOfLengthBetween(2, 5);
            LifecyclePolicy policy = createRandomPolicy(policyName);
            policies.put(policyName, new LifecyclePolicyMetadata(policy, randomLong(), randomLong()));
        }
        return new GetLifecyclePolicyResponse(policies.build());
    }

    @Override
    protected GetLifecyclePolicyResponse doParseInstance(XContentParser parser) throws IOException {
        return GetLifecyclePolicyResponse.fromXContent(parser);
    }

    @Override
    protected boolean supportsUnknownFields() {
        return true;
    }

    @Override
    protected Predicate<String> getRandomFieldsExcludeFilter() {
        return (field) ->
            // phases is a list of Phase parsable entries only
            field.endsWith(".phases")
            // these are all meant to be maps of strings, so complex objects will confuse the parser
            || field.endsWith(".include")
            || field.endsWith(".exclude")
            || field.endsWith(".require")
            // actions are meant to be a list of LifecycleAction parsable entries only
            || field.endsWith(".actions")
            // field.isEmpty() means do not insert an object at the root of the json. This parser expects
            // every root level named object to be parsable as a specific type
            || field.isEmpty();
    }

    @Override
    protected NamedXContentRegistry xContentRegistry() {
        List<NamedXContentRegistry.Entry> entries = new ArrayList<>(ClusterModule.getNamedXWriteables());
        entries.addAll(Arrays.asList(
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(AllocateAction.NAME), AllocateAction::parse),
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
}
