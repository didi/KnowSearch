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

package org.elasticsearch.action.get;

import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Predicate;

import static org.elasticsearch.common.xcontent.XContentHelper.toXContent;
import static org.elasticsearch.index.get.GetResultTests.copyGetResult;
import static org.elasticsearch.index.get.GetResultTests.mutateGetResult;
import static org.elasticsearch.index.get.GetResultTests.randomGetResult;
import static org.elasticsearch.index.seqno.SequenceNumbers.UNASSIGNED_SEQ_NO;
import static org.elasticsearch.test.EqualsHashCodeTestUtils.checkEqualsAndHashCode;
import static org.elasticsearch.test.XContentTestUtils.insertRandomFields;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertToXContentEquivalent;

public class GetResponseTests extends ESTestCase {

    public void testToAndFromXContent() throws Exception {
        doFromXContentTestWithRandomFields(false);
    }

    /**
     * This test adds random fields and objects to the xContent rendered out to
     * ensure we can parse it back to be forward compatible with additions to
     * the xContent
     */
    public void testFromXContentWithRandomFields() throws IOException {
        doFromXContentTestWithRandomFields(true);
    }

    private void doFromXContentTestWithRandomFields(boolean addRandomFields) throws IOException {
        XContentType xContentType = randomFrom(XContentType.values());
        Tuple<GetResult, GetResult> tuple = randomGetResult(xContentType);
        GetResponse getResponse = new GetResponse(tuple.v1());
        GetResponse expectedGetResponse = new GetResponse(tuple.v2());
        boolean humanReadable = randomBoolean();
        BytesReference originalBytes = toShuffledXContent(getResponse, xContentType, ToXContent.EMPTY_PARAMS, humanReadable, "_source");

        BytesReference mutated;
        if (addRandomFields) {
            // "_source" and "fields" just consists of key/value pairs, we shouldn't add anything random there. It is already
            // randomized in the randomGetResult() method anyway. Also, we cannot add anything in the root object since this is
            // where GetResult's metadata fields are rendered out while            // other fields are rendered out in a "fields" object.
            Predicate<String> excludeFilter = (s) -> s.isEmpty() || s.contains("fields") || s.contains("_source");
            mutated = insertRandomFields(xContentType, originalBytes, excludeFilter, random());
        } else {
            mutated = originalBytes;
        }
        GetResponse parsedGetResponse;
        try (XContentParser parser = createParser(xContentType.xContent(), mutated)) {
            parsedGetResponse = GetResponse.fromXContent(parser);
            assertNull(parser.nextToken());
        }
        assertEquals(expectedGetResponse.getSourceAsMap(), parsedGetResponse.getSourceAsMap());
        //print the parsed object out and test that the output is the same as the original output
        BytesReference finalBytes = toXContent(parsedGetResponse, xContentType, humanReadable);
        assertToXContentEquivalent(originalBytes, finalBytes, xContentType);
        //check that the source stays unchanged, no shuffling of keys nor anything like that
        assertEquals(expectedGetResponse.getSourceAsString(), parsedGetResponse.getSourceAsString());
    }

    public void testToXContent() {
        {
            GetResponse getResponse = new GetResponse(new GetResult("index", "type", "id", 0, 1, 1, true, new BytesArray("{ \"field1\" : " +
                    "\"value1\", \"field2\":\"value2\"}"), Collections.singletonMap("field1", new DocumentField("field1",
                    Collections.singletonList("value1"))), null));
            String output = Strings.toString(getResponse);
            assertEquals("{\"_index\":\"index\",\"_type\":\"type\",\"_id\":\"id\",\"_version\":1,\"_seq_no\":0,\"_primary_term\":1," +
                "\"found\":true,\"_source\":{ \"field1\" : \"value1\", \"field2\":\"value2\"},\"fields\":{\"field1\":[\"value1\"]}}",
                output);
        }
        {
            GetResponse getResponse = new GetResponse(new GetResult("index", "type", "id", UNASSIGNED_SEQ_NO,
                0, 1, false, null, null, null));
            String output = Strings.toString(getResponse);
            assertEquals("{\"_index\":\"index\",\"_type\":\"type\",\"_id\":\"id\",\"found\":false}", output);
        }
    }

    public void testToString() {
        GetResponse getResponse = new GetResponse(new GetResult("index", "type", "id", 0, 1, 1, true,
                    new BytesArray("{ \"field1\" : " + "\"value1\", \"field2\":\"value2\"}"),
                        Collections.singletonMap("field1", new DocumentField("field1", Collections.singletonList("value1"))), null));
        assertEquals("{\"_index\":\"index\",\"_type\":\"type\",\"_id\":\"id\",\"_version\":1,\"_seq_no\":0,\"_primary_term\":1," +
            "\"found\":true,\"_source\":{ \"field1\" : \"value1\", \"field2\":\"value2\"},\"fields\":{\"field1\":[\"value1\"]}}",
            getResponse.toString());
    }

    public void testEqualsAndHashcode() {
        checkEqualsAndHashCode(new GetResponse(randomGetResult(XContentType.JSON).v1()), GetResponseTests::copyGetResponse,
                GetResponseTests::mutateGetResponse);
    }

    public void testFromXContentThrowsParsingException() throws IOException {
        GetResponse getResponse =
            new GetResponse(new GetResult(null, null, null, UNASSIGNED_SEQ_NO, 0, randomIntBetween(1, 5),
                randomBoolean(), null, null, null));

        XContentType xContentType = randomFrom(XContentType.values());
        BytesReference originalBytes = toShuffledXContent(getResponse, xContentType, ToXContent.EMPTY_PARAMS, randomBoolean());

        try (XContentParser parser = createParser(xContentType.xContent(), originalBytes)) {
            ParsingException exception = expectThrows(ParsingException.class, () -> GetResponse.fromXContent(parser));
            assertEquals("Missing required fields [_index,_type,_id]", exception.getMessage());
        }
    }

    private static GetResponse copyGetResponse(GetResponse getResponse) {
        return new GetResponse(copyGetResult(getResponse.getResult));
    }

    private static GetResponse mutateGetResponse(GetResponse getResponse) {
        return new GetResponse(mutateGetResult(getResponse.getResult));
    }
}
