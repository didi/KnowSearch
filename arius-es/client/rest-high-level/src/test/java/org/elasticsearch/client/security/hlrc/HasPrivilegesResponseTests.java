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

package org.elasticsearch.client.security.hlrc;

import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.Version;
import org.elasticsearch.client.AbstractResponseTestCase;
import org.elasticsearch.client.security.HasPrivilegesResponse;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.util.set.Sets;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.VersionUtils;
import org.elasticsearch.xpack.core.security.authz.permission.ResourcePrivileges;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;

public class HasPrivilegesResponseTests extends AbstractResponseTestCase<
    org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse,
    HasPrivilegesResponse> {

    public void testSerializationV64OrV65() throws IOException {
        final org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse original = randomResponse();
        final Version version = VersionUtils.randomVersionBetween(LuceneTestCase.random(), Version.V_6_4_0, Version.V_6_5_1);
        final org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse copy = serializeAndDeserialize(original, version);

        Assert.assertThat(copy.isCompleteMatch(), equalTo(original.isCompleteMatch()));
        Assert.assertThat(copy.getClusterPrivileges().entrySet(), Matchers.emptyIterable());
        Assert.assertThat(copy.getIndexPrivileges(), equalTo(original.getIndexPrivileges()));
        Assert.assertThat(copy.getApplicationPrivileges(), equalTo(original.getApplicationPrivileges()));
    }

    public void testSerializationV63() throws IOException {
        final org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse original = randomResponse();
        final org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse copy =
            serializeAndDeserialize(original, Version.V_6_3_0);

        Assert.assertThat(copy.isCompleteMatch(), equalTo(original.isCompleteMatch()));
        Assert.assertThat(copy.getClusterPrivileges().entrySet(), Matchers.emptyIterable());
        Assert.assertThat(copy.getIndexPrivileges(), equalTo(original.getIndexPrivileges()));
        Assert.assertThat(copy.getApplicationPrivileges(), equalTo(Collections.emptyMap()));
    }

    public void testToXContent() throws Exception {
        final org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse response =
            new org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse("daredevil",
                false, Collections.singletonMap("manage", true),
                Arrays.asList(
                        ResourcePrivileges.builder("staff")
                                .addPrivileges(MapBuilder.<String, Boolean>newMapBuilder(new LinkedHashMap<>()).put("read", true)
                                        .put("index", true).put("delete", false).put("manage", false).map())
                                .build(),
                        ResourcePrivileges.builder("customers")
                                .addPrivileges(MapBuilder.<String, Boolean>newMapBuilder(new LinkedHashMap<>()).put("read", true)
                                        .put("index", true).put("delete", true).put("manage", false).map())
                                .build()),
                Collections.emptyMap());

        final XContentBuilder builder = XContentBuilder.builder(XContentType.JSON.xContent());
        response.toXContent(builder, ToXContent.EMPTY_PARAMS);
        BytesReference bytes = BytesReference.bytes(builder);

        final String json = bytes.utf8ToString();
        Assert.assertThat(json, equalTo("{" +
            "\"username\":\"daredevil\"," +
            "\"has_all_requested\":false," +
            "\"cluster\":{\"manage\":true}," +
            "\"index\":{" +
            "\"customers\":{\"read\":true,\"index\":true,\"delete\":true,\"manage\":false}," +
            "\"staff\":{\"read\":true,\"index\":true,\"delete\":false,\"manage\":false}" +
            "}," +
            "\"application\":{}" +
            "}"));
    }

    @Override
    protected org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse createServerTestInstance(XContentType xContentType) {
        return randomResponse();
    }

    @Override
    protected HasPrivilegesResponse doParseToClientInstance(XContentParser parser) throws IOException {
        return HasPrivilegesResponse.fromXContent(parser);
    }

    private static List<ResourcePrivileges> toResourcePrivileges(Map<String, Map<String, Boolean>> map) {
        return map.entrySet().stream()
            .map(e -> ResourcePrivileges.builder(e.getKey()).addPrivileges(e.getValue()).build())
            .collect(Collectors.toList());
    }

    private org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse serializeAndDeserialize(
        org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse original, Version version) throws IOException {
        logger.info("Test serialize/deserialize with version {}", version);
        final BytesStreamOutput out = new BytesStreamOutput();
        out.setVersion(version);
        original.writeTo(out);

        final StreamInput in = out.bytes().streamInput();
        in.setVersion(version);
        final org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse copy =
            new org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse(in);
        Assert.assertThat(in.read(), equalTo(-1));
        return copy;
    }

    private org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse randomResponse() {
        final String username = randomAlphaOfLengthBetween(4, 12);
        final Map<String, Boolean> cluster = new HashMap<>();
        for (String priv : randomArray(1, 6, String[]::new, () -> randomAlphaOfLengthBetween(3, 12))) {
            cluster.put(priv, randomBoolean());
        }
        final Collection<ResourcePrivileges> index = randomResourcePrivileges();
        final Map<String, Collection<ResourcePrivileges>> application = new HashMap<>();
        for (String app : randomArray(1, 3, String[]::new,
            () -> randomAlphaOfLengthBetween(3, 6).toLowerCase(Locale.ROOT))) {
            application.put(app, randomResourcePrivileges());
        }
        return new org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse(username, randomBoolean(),
            cluster, index, application);
    }

    private Collection<ResourcePrivileges> randomResourcePrivileges() {
        final Collection<ResourcePrivileges> list = new ArrayList<>();
        // Use hash set to force a unique set of resources
        for (String resource : Sets.newHashSet(randomArray(1, 3, String[]::new,
            () -> randomAlphaOfLengthBetween(2, 6)))) {
            final Map<String, Boolean> privileges = new HashMap<>();
            for (String priv : randomArray(1, 5, String[]::new, () -> randomAlphaOfLengthBetween(3, 8))) {
                privileges.put(priv, randomBoolean());
            }
            list.add(ResourcePrivileges.builder(resource).addPrivileges(privileges).build());
        }
        return list;
    }

    @Override
    protected void assertInstances(org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse serverTestInstance,
                                   HasPrivilegesResponse hlrc) {
        org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse other =
            new org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse(
                hlrc.getUsername(),
                hlrc.hasAllRequested(),
                hlrc.getClusterPrivileges(),
                toResourcePrivileges(hlrc.getIndexPrivileges()),
                hlrc.getApplicationPrivileges().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> toResourcePrivileges(e.getValue())))
        );
        assertEquals(serverTestInstance, other);
    }
}
