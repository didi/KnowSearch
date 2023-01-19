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

package org.elasticsearch.action.index;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.client.NoOpClient;
import org.junit.After;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class IndexRequestBuilderTests extends ESTestCase {

    private static final String EXPECTED_SOURCE = "{\"SomeKey\":\"SomeValue\"}";
    private NoOpClient testClient;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.testClient = new NoOpClient(getTestName());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        this.testClient.close();
        super.tearDown();
    }

    /**
     * test setting the source for the request with different available setters
     */
    public void testSetSource() throws Exception {
        IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(this.testClient, IndexAction.INSTANCE);
        Map<String, String> source = new HashMap<>();
        source.put("SomeKey", "SomeValue");
        indexRequestBuilder.setSource(source);
        assertEquals(EXPECTED_SOURCE, XContentHelper.convertToJson(indexRequestBuilder.request().source(), true));

        indexRequestBuilder.setSource(source, XContentType.JSON);
        assertEquals(EXPECTED_SOURCE, XContentHelper.convertToJson(indexRequestBuilder.request().source(), true));

        indexRequestBuilder.setSource("SomeKey", "SomeValue");
        assertEquals(EXPECTED_SOURCE, XContentHelper.convertToJson(indexRequestBuilder.request().source(), true));

        // force the Object... setter
        indexRequestBuilder.setSource((Object) "SomeKey", "SomeValue");
        assertEquals(EXPECTED_SOURCE, XContentHelper.convertToJson(indexRequestBuilder.request().source(), true));

        ByteArrayOutputStream docOut = new ByteArrayOutputStream();
        XContentBuilder doc = XContentFactory.jsonBuilder(docOut).startObject().field("SomeKey", "SomeValue").endObject();
        doc.close();
        indexRequestBuilder.setSource(docOut.toByteArray(), XContentType.JSON);
        assertEquals(EXPECTED_SOURCE, XContentHelper.convertToJson(indexRequestBuilder.request().source(), true,
            indexRequestBuilder.request().getContentType()));

        doc = XContentFactory.jsonBuilder().startObject().field("SomeKey", "SomeValue").endObject();
        doc.close();
        indexRequestBuilder.setSource(doc);
        assertEquals(EXPECTED_SOURCE, XContentHelper.convertToJson(indexRequestBuilder.request().source(), true));
    }
}
