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

package org.elasticsearch.rest.action;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.rest.FakeRestChannel;
import org.elasticsearch.test.rest.FakeRestRequest;
import org.elasticsearch.transport.TransportResponse;
import org.elasticsearch.transport.TransportResponse.Empty;

import java.util.concurrent.atomic.AtomicReference;

public class RestBuilderListenerTests extends ESTestCase {

    public void testXContentBuilderClosedInBuildResponse() throws Exception {
        AtomicReference<XContentBuilder> builderAtomicReference = new AtomicReference<>();
        RestBuilderListener<TransportResponse.Empty> builderListener =
            new RestBuilderListener<Empty>(new FakeRestChannel(new FakeRestRequest(), randomBoolean(), 1)) {
                @Override
                public RestResponse buildResponse(Empty empty, XContentBuilder builder) throws Exception {
                    builderAtomicReference.set(builder);
                    builder.close();
                    return new BytesRestResponse(RestStatus.OK, BytesRestResponse.TEXT_CONTENT_TYPE, BytesArray.EMPTY);
                }
        };

        builderListener.buildResponse(Empty.INSTANCE);
        assertNotNull(builderAtomicReference.get());
        assertTrue(builderAtomicReference.get().generator().isClosed());
    }

    public void testXContentBuilderNotClosedInBuildResponseAssertionsDisabled() throws Exception {
        AtomicReference<XContentBuilder> builderAtomicReference = new AtomicReference<>();
        RestBuilderListener<TransportResponse.Empty> builderListener =
            new RestBuilderListener<Empty>(new FakeRestChannel(new FakeRestRequest(), randomBoolean(), 1)) {
                @Override
                public RestResponse buildResponse(Empty empty, XContentBuilder builder) throws Exception {
                    builderAtomicReference.set(builder);
                    return new BytesRestResponse(RestStatus.OK, BytesRestResponse.TEXT_CONTENT_TYPE, BytesArray.EMPTY);
                }

                @Override
                boolean assertBuilderClosed(XContentBuilder xContentBuilder) {
                    // don't check the actual builder being closed so we can test auto close
                    return true;
                }
        };

        builderListener.buildResponse(Empty.INSTANCE);
        assertNotNull(builderAtomicReference.get());
        assertTrue(builderAtomicReference.get().generator().isClosed());
    }

    public void testXContentBuilderNotClosedInBuildResponseAssertionsEnabled() throws Exception {
        assumeTrue("tests are not being run with assertions", RestBuilderListener.class.desiredAssertionStatus());

        RestBuilderListener<TransportResponse.Empty> builderListener =
            new RestBuilderListener<Empty>(new FakeRestChannel(new FakeRestRequest(), randomBoolean(), 1)) {
                @Override
                public RestResponse buildResponse(Empty empty, XContentBuilder builder) throws Exception {
                    return new BytesRestResponse(RestStatus.OK, BytesRestResponse.TEXT_CONTENT_TYPE, BytesArray.EMPTY);
                }
        };

        AssertionError error = expectThrows(AssertionError.class, () -> builderListener.buildResponse(Empty.INSTANCE));
        assertEquals("callers should ensure the XContentBuilder is closed themselves", error.getMessage());
    }
}
