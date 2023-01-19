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

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MultiGetResponseTests extends ESTestCase {

    public void testFromXContent() throws IOException {
        for (int runs = 0; runs < 20; runs++) {
            MultiGetResponse expected = createTestInstance();
            XContentType xContentType = randomFrom(XContentType.values());
            BytesReference shuffled = toShuffledXContent(expected, xContentType, ToXContent.EMPTY_PARAMS, false);
            MultiGetResponse parsed;
            try (XContentParser parser = createParser(XContentFactory.xContent(xContentType), shuffled)) {
                parsed = MultiGetResponse.fromXContent(parser);
                assertNull(parser.nextToken());
            }
            assertNotSame(expected, parsed);

            assertThat(parsed.getResponses().length, equalTo(expected.getResponses().length));
            for (int i = 0; i < expected.getResponses().length; i++) {
                MultiGetItemResponse expectedItem = expected.getResponses()[i];
                MultiGetItemResponse actualItem = parsed.getResponses()[i];
                assertThat(actualItem.getIndex(), equalTo(expectedItem.getIndex()));
                assertThat(actualItem.getType(), equalTo(expectedItem.getType()));
                assertThat(actualItem.getId(), equalTo(expectedItem.getId()));
                if (expectedItem.isFailed()) {
                    assertThat(actualItem.isFailed(), is(true));
                    assertThat(actualItem.getFailure().getMessage(), containsString(expectedItem.getFailure().getMessage()));
                } else {
                    assertThat(actualItem.isFailed(), is(false));
                    assertThat(actualItem.getResponse(), equalTo(expectedItem.getResponse()));
                }
            }

        }
    }

    private static MultiGetResponse createTestInstance() {
        MultiGetItemResponse[] items = new MultiGetItemResponse[randomIntBetween(0, 128)];
        for (int i = 0; i < items.length; i++) {
            if (randomBoolean()) {
                items[i] = new MultiGetItemResponse(new GetResponse(new GetResult(
                        randomAlphaOfLength(4), randomAlphaOfLength(4), randomAlphaOfLength(4), 0, 1, randomNonNegativeLong(),
                        true, null, null, null
                )), null);
            } else {
                items[i] = new MultiGetItemResponse(null, new MultiGetResponse.Failure(randomAlphaOfLength(4),
                        randomAlphaOfLength(4), randomAlphaOfLength(4), new RuntimeException(randomAlphaOfLength(4))));
            }
        }
        return new MultiGetResponse(items);
    }

}
