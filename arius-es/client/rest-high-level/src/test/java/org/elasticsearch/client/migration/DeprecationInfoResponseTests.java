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

package org.elasticsearch.client.migration;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.EqualsHashCodeTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.client.migration.DeprecationInfoResponse.DeprecationIssue.Level.CRITICAL;
import static org.elasticsearch.client.migration.DeprecationInfoResponse.DeprecationIssue.Level.WARNING;
import static org.elasticsearch.test.AbstractXContentTestCase.xContentTester;

public class DeprecationInfoResponseTests extends ESTestCase {

    private void toXContent(DeprecationInfoResponse response, XContentBuilder builder) throws IOException {
        builder.startObject();
        {
            builder.startArray("cluster_settings");
            for (DeprecationInfoResponse.DeprecationIssue issue : response.getClusterSettingsIssues()) {
                toXContent(issue, builder);
            }
            builder.endArray();

            builder.startArray("node_settings");
            for (DeprecationInfoResponse.DeprecationIssue issue : response.getNodeSettingsIssues()) {
                toXContent(issue, builder);
            }
            builder.endArray();

            builder.field("index_settings");
            builder.startObject();
            {
                for (Map.Entry<String, List<DeprecationInfoResponse.DeprecationIssue>> entry :
                        response.getIndexSettingsIssues().entrySet()) {
                    builder.field(entry.getKey());
                    builder.startArray();
                    for (DeprecationInfoResponse.DeprecationIssue issue : entry.getValue()) {
                        toXContent(issue, builder);
                    }
                    builder.endArray();
                }
            }
            builder.endObject();

            builder.startArray("ml_settings");
            for (DeprecationInfoResponse.DeprecationIssue issue : response.getMlSettingsIssues()) {
                toXContent(issue, builder);
            }
            builder.endArray();
        }
        builder.endObject();
    }

    private void toXContent(DeprecationInfoResponse.DeprecationIssue issue, XContentBuilder builder) throws IOException {
        builder.startObject()
            .field("level", issue.getLevel())
            .field("message", issue.getMessage())
            .field("url", issue.getUrl());
        if (issue.getDetails()!= null) {
            builder.field("details", issue.getDetails());
        }
        builder.endObject();
    }


    private Map<String, List<DeprecationInfoResponse.DeprecationIssue>> createIndexSettingsIssues() {
        Map<String, List<DeprecationInfoResponse.DeprecationIssue>> indexSettingsIssues =
            new HashMap<>();
        for (int i = 0; i < randomIntBetween(1, 3); i++) {
            indexSettingsIssues.put(randomAlphaOfLengthBetween(1, 5), createRandomIssues(false));
        }
        return indexSettingsIssues;
    }

    private List<DeprecationInfoResponse.DeprecationIssue> createRandomIssues(boolean canBeEmpty) {
        List<DeprecationInfoResponse.DeprecationIssue> list = new ArrayList<>();
        // the list of index settings cannot be zero, but the other lists can be, so this boolean is used to make the min number
        // of elements for this list.
        int startingRandomNumber = canBeEmpty ? 0: 1;
        for (int i =0; i < randomIntBetween(startingRandomNumber, 2); i++) {
            list.add(new DeprecationInfoResponse.DeprecationIssue(randomFrom(WARNING, CRITICAL),
                randomAlphaOfLength(5),
                randomAlphaOfLength(5),
                randomBoolean() ? randomAlphaOfLength(5) : null));
        }
        return list;
    }

    private DeprecationInfoResponse createInstance() {
        return new DeprecationInfoResponse(createRandomIssues(true), createRandomIssues(true), createIndexSettingsIssues(),
            createRandomIssues(true));
    }

    private DeprecationInfoResponse copyInstance(DeprecationInfoResponse req) {
        return new DeprecationInfoResponse(new ArrayList<>(req.getClusterSettingsIssues()),
            new ArrayList<>(req.getNodeSettingsIssues()), new HashMap<>(req.getIndexSettingsIssues()),
            new ArrayList<>(req.getMlSettingsIssues()));
    }

    private DeprecationInfoResponse mutateInstance(DeprecationInfoResponse req) {
        return createInstance();
    }

    public void testFromXContent() throws IOException {
        xContentTester(
            this::createParser,
            this::createInstance,
            this::toXContent,
            DeprecationInfoResponse::fromXContent)
            .supportsUnknownFields(false) // old school parsing
            .test();
    }

    public void testNullFailedIndices() {
        NullPointerException exception = expectThrows(NullPointerException.class,
            () -> new DeprecationInfoResponse(null, null, null, null));
        assertEquals("cluster settings issues cannot be null", exception.getMessage());

        exception = expectThrows(NullPointerException.class,
            () -> new DeprecationInfoResponse(Collections.emptyList(), null, null, null));
        assertEquals("node settings issues cannot be null", exception.getMessage());

        exception = expectThrows(NullPointerException.class,
            () -> new DeprecationInfoResponse(Collections.emptyList(), Collections.emptyList(), null, null));
        assertEquals("index settings issues cannot be null", exception.getMessage());

        exception = expectThrows(NullPointerException.class,
            () -> new DeprecationInfoResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), null));
        assertEquals("ml settings issues cannot be null", exception.getMessage());
    }

    public void testEqualsAndHashCode() {
        for (int count = 0; count < 100; ++count) {
            EqualsHashCodeTestUtils.checkEqualsAndHashCode(createInstance(), this::copyInstance, this::mutateInstance);
        }
    }
}
