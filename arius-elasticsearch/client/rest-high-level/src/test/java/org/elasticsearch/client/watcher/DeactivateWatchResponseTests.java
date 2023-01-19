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
package org.elasticsearch.client.watcher;


import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;

public class DeactivateWatchResponseTests extends ESTestCase {

    public void testBasicParsing() throws IOException {
        XContentType contentType = randomFrom(XContentType.values());
        int version = randomInt();
        ExecutionState executionState = randomFrom(ExecutionState.values());
        XContentBuilder builder = XContentFactory.contentBuilder(contentType).startObject()
            .startObject("status")
            .field("version", version)
            .field("execution_state", executionState)
            .endObject()
            .endObject();
        BytesReference bytes = BytesReference.bytes(builder);
        DeactivateWatchResponse response = parse(contentType, bytes);
        WatchStatus status = response.getStatus();
        assertNotNull(status);
        assertEquals(version, status.version());
        assertEquals(executionState, status.getExecutionState());
    }

    private DeactivateWatchResponse parse(XContentType contentType, BytesReference bytes) throws IOException {
        XContentParser parser = XContentFactory.xContent(contentType)
            .createParser(NamedXContentRegistry.EMPTY, null, bytes.streamInput());
        parser.nextToken();
        return DeactivateWatchResponse.fromXContent(parser);
    }
}
