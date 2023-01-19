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
package org.elasticsearch.client.security;

import org.elasticsearch.client.security.user.privileges.ApplicationResourcePrivileges;
import org.elasticsearch.client.security.user.privileges.IndicesPrivileges;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.util.set.Sets;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.EqualsHashCodeTestUtils;
import org.elasticsearch.test.XContentTestUtils;
import org.hamcrest.Matchers;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class HasPrivilegesRequestTests extends ESTestCase {

    public void testToXContent() throws IOException {
        final HasPrivilegesRequest request = new HasPrivilegesRequest(
            new LinkedHashSet<>(Arrays.asList("monitor", "manage_watcher", "manage_ml")),
            new LinkedHashSet<>(Arrays.asList(
                IndicesPrivileges.builder().indices("index-001", "index-002").privileges("all")
                    .allowRestrictedIndices(true).build(),
                IndicesPrivileges.builder().indices("index-003").privileges("read")
                    .build()
            )),
            new LinkedHashSet<>(Arrays.asList(
                new ApplicationResourcePrivileges("myapp", Arrays.asList("read", "write"), Arrays.asList("*")),
                new ApplicationResourcePrivileges("myapp", Arrays.asList("admin"), Arrays.asList("/data/*"))
            ))
        );
        String json = Strings.toString(request);
        final Map<String, Object> parsed = XContentHelper.convertToMap(XContentType.JSON.xContent(), json, false);

        final Map<String, Object> expected = XContentHelper.convertToMap(XContentType.JSON.xContent(), "{" +
            " \"cluster\":[\"monitor\",\"manage_watcher\",\"manage_ml\"]," +
            " \"index\":[{" +
            "   \"names\":[\"index-001\",\"index-002\"]," +
            "   \"privileges\":[\"all\"]," +
            "   \"allow_restricted_indices\":true" +
            "  },{" +
            "   \"names\":[\"index-003\"]," +
            "   \"privileges\":[\"read\"]," +
            "   \"allow_restricted_indices\":false" +
            " }]," +
            " \"application\":[{" +
            "   \"application\":\"myapp\"," +
            "   \"privileges\":[\"read\",\"write\"]," +
            "   \"resources\":[\"*\"]" +
            "  },{" +
            "   \"application\":\"myapp\"," +
            "   \"privileges\":[\"admin\"]," +
            "   \"resources\":[\"/data/*\"]" +
            " }]" +
            "}", false);

        assertThat(XContentTestUtils.differenceBetweenMapsIgnoringArrayOrder(parsed, expected), Matchers.nullValue());
    }

    public void testEqualsAndHashCode() {
        final Set<String> cluster = Sets.newHashSet(randomArray(1, 3, String[]::new, () -> randomAlphaOfLengthBetween(3, 8)));
        final Set<IndicesPrivileges> indices = Sets.newHashSet(randomArray(1, 5, IndicesPrivileges[]::new,
            () -> IndicesPrivileges.builder()
                .indices(generateRandomStringArray(5, 12, false, false))
                .privileges(generateRandomStringArray(3, 8, false, false))
                .allowRestrictedIndices(randomBoolean())
                .build()));
        final Set<ApplicationResourcePrivileges> application = Sets.newHashSet(randomArray(1, 5, ApplicationResourcePrivileges[]::new,
            () -> new ApplicationResourcePrivileges(
                randomAlphaOfLengthBetween(5, 12),
                Sets.newHashSet(generateRandomStringArray(3, 8, false, false)),
                Sets.newHashSet(generateRandomStringArray(2, 6, false, false))
            )));
        final HasPrivilegesRequest request = new HasPrivilegesRequest(cluster, indices, application);
        EqualsHashCodeTestUtils.checkEqualsAndHashCode(request, this::copy, this::mutate);
    }

    private HasPrivilegesRequest copy(HasPrivilegesRequest request) {
        return new HasPrivilegesRequest(request.getClusterPrivileges(), request.getIndexPrivileges(), request.getApplicationPrivileges());
    }

    private HasPrivilegesRequest mutate(HasPrivilegesRequest request) {
        switch (randomIntBetween(1, 3)) {
            case 1:
                return new HasPrivilegesRequest(null, request.getIndexPrivileges(), request.getApplicationPrivileges());
            case 2:
                return new HasPrivilegesRequest(request.getClusterPrivileges(), null, request.getApplicationPrivileges());
            case 3:
                return new HasPrivilegesRequest(request.getClusterPrivileges(), request.getIndexPrivileges(), null);
        }
        throw new IllegalStateException("The universe is broken (or the RNG is)");
    }

}
