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

package org.elasticsearch.rest.action.admin.indices;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.test.rest.FakeRestChannel;
import org.elasticsearch.test.rest.FakeRestRequest;
import org.elasticsearch.test.rest.RestActionTestCase;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.rest.BaseRestHandler.INCLUDE_TYPE_NAME_PARAMETER;

public class RestGetFieldMappingActionTests extends RestActionTestCase {

    @Before
    public void setUpAction() {
        new RestGetFieldMappingAction(controller());
    }

    public void testIncludeTypeName() {
        Map<String, String> params = new HashMap<>();
        String path;
        if (randomBoolean()) {
            params.put(INCLUDE_TYPE_NAME_PARAMETER, "true");
            path = "some_index/some_type/_mapping/field/some_field";
        } else {
            params.put(INCLUDE_TYPE_NAME_PARAMETER, "false");
            path = "some_index/_mapping/field/some_field";
        }
        RestRequest deprecatedRequest = new FakeRestRequest.Builder(xContentRegistry())
            .withMethod(RestRequest.Method.GET)
            .withPath(path)
            .withParams(params)
            .build();
        dispatchRequest(deprecatedRequest);
        assertWarnings(RestGetFieldMappingAction.TYPES_DEPRECATION_MESSAGE);

        RestRequest validRequest = new FakeRestRequest.Builder(xContentRegistry())
            .withMethod(RestRequest.Method.GET)
            .withPath("some_index/_mapping/field/some_field")
            .build();
        dispatchRequest(validRequest);
    }

    public void testTypeInPath() {
        // Test that specifying a type while setting include_type_name to false
        // results in an illegal argument exception.
        Map<String, String> params = new HashMap<>();
        params.put(INCLUDE_TYPE_NAME_PARAMETER, "false");
        RestRequest request = new FakeRestRequest.Builder(xContentRegistry())
            .withMethod(RestRequest.Method.GET)
            .withPath("some_index/some_type/_mapping/field/some_field")
            .withParams(params)
            .build();

        FakeRestChannel channel = new FakeRestChannel(request, false, 1);
        ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        controller().dispatchRequest(request, channel, threadContext);

        assertEquals(1, channel.errors().get());
        assertEquals(RestStatus.BAD_REQUEST, channel.capturedResponse().status());
    }
}
