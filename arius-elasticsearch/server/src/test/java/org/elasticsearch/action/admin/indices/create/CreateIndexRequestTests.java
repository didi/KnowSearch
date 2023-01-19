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

package org.elasticsearch.action.admin.indices.create;

import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.RandomCreateIndexGenerator;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.hamcrest.ElasticsearchAssertions;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_SHARDS;
import static org.elasticsearch.common.xcontent.ToXContent.EMPTY_PARAMS;
import static org.hamcrest.CoreMatchers.equalTo;

public class CreateIndexRequestTests extends ESTestCase {

    public void testSerialization() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("foo");
        String mapping = Strings.toString(JsonXContent.contentBuilder().startObject().startObject("my_type").endObject().endObject());
        request.mapping("my_type", mapping, XContentType.JSON);

        try (BytesStreamOutput output = new BytesStreamOutput()) {
            request.writeTo(output);

            try (StreamInput in = output.bytes().streamInput()) {
                CreateIndexRequest serialized = new CreateIndexRequest(in);
                assertEquals(request.index(), serialized.index());
                assertEquals(mapping, serialized.mappings().get("my_type"));
            }
        }
    }

    public void testTopLevelKeys() {
        String createIndex =
                "{\n"
                + "  \"FOO_SHOULD_BE_ILLEGAL_HERE\": {\n"
                + "    \"BAR_IS_THE_SAME\": 42\n"
                + "  },\n"
                + "  \"mappings\": {\n"
                + "    \"test\": {\n"
                + "      \"properties\": {\n"
                + "        \"field1\": {\n"
                + "          \"type\": \"text\"\n"
                + "       }\n"
                + "     }\n"
                + "    }\n"
                + "  }\n"
                + "}";

        CreateIndexRequest request = new CreateIndexRequest();
        ElasticsearchParseException e = expectThrows(ElasticsearchParseException.class,
                () -> {request.source(createIndex, XContentType.JSON);});
        assertEquals("unknown key [FOO_SHOULD_BE_ILLEGAL_HERE] for create index", e.getMessage());
    }

    public void testToXContent() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("foo");

        String mapping;
        if (randomBoolean()) {
            mapping = Strings.toString(JsonXContent.contentBuilder().startObject().startObject("my_type").endObject().endObject());
        } else {
            mapping = Strings.toString(JsonXContent.contentBuilder().startObject().endObject());
        }
        request.mapping("my_type", mapping, XContentType.JSON);

        Alias alias = new Alias("test_alias");
        alias.routing("1");
        alias.filter("{\"term\":{\"year\":2016}}");
        alias.writeIndex(true);
        request.alias(alias);

        Settings.Builder settings = Settings.builder();
        settings.put(SETTING_NUMBER_OF_SHARDS, 10);
        request.settings(settings);

        String actualRequestBody = Strings.toString(request);

        String expectedRequestBody = "{\"settings\":{\"index\":{\"number_of_shards\":\"10\"}}," +
            "\"mappings\":{\"my_type\":{\"my_type\":{}}}," +
            "\"aliases\":{\"test_alias\":{\"filter\":{\"term\":{\"year\":2016}},\"routing\":\"1\",\"is_write_index\":true}}}";

        assertEquals(expectedRequestBody, actualRequestBody);
    }

    public void testMappingKeyedByType() throws IOException {
        CreateIndexRequest request1 = new CreateIndexRequest("foo");
        CreateIndexRequest request2 = new CreateIndexRequest("bar");
        {
            XContentBuilder builder = XContentFactory.contentBuilder(randomFrom(XContentType.values()));
            builder.startObject().startObject("properties")
                .startObject("field1")
                    .field("type", "text")
                .endObject()
                .startObject("field2")
                    .startObject("properties")
                        .startObject("field21")
                            .field("type", "keyword")
                        .endObject()
                    .endObject()
                .endObject()
            .endObject().endObject();
            request1.mapping("type1", builder);
            builder = XContentFactory.contentBuilder(randomFrom(XContentType.values()));
            builder.startObject().startObject("type1")
                .startObject("properties")
                    .startObject("field1")
                        .field("type", "text")
                    .endObject()
                    .startObject("field2")
                        .startObject("properties")
                            .startObject("field21")
                                .field("type", "keyword")
                            .endObject()
                        .endObject()
                    .endObject()
                .endObject()
            .endObject().endObject();
            request2.mapping("type1", builder);
            assertEquals(request1.mappings(), request2.mappings());
        }
        {
            request1 = new CreateIndexRequest("foo");
            request2 = new CreateIndexRequest("bar");
            String nakedMapping = "{\"properties\": {\"foo\": {\"type\": \"integer\"}}}";
            request1.mapping("type2", nakedMapping, XContentType.JSON);
            request2.mapping("type2", "{\"type2\": " + nakedMapping + "}", XContentType.JSON);
            assertEquals(request1.mappings(), request2.mappings());
        }
        {
            request1 = new CreateIndexRequest("foo");
            request2 = new CreateIndexRequest("bar");
            Map<String , Object> nakedMapping = MapBuilder.<String, Object>newMapBuilder()
                    .put("properties", MapBuilder.<String, Object>newMapBuilder()
                            .put("bar", MapBuilder.<String, Object>newMapBuilder()
                                    .put("type", "scaled_float")
                                    .put("scaling_factor", 100)
                            .map())
                    .map())
            .map();
            request1.mapping("type3", nakedMapping);
            request2.mapping("type3", MapBuilder.<String, Object>newMapBuilder().put("type3", nakedMapping).map());
            assertEquals(request1.mappings(), request2.mappings());
        }
    }

    public void testToAndFromXContent() throws IOException {

        final CreateIndexRequest createIndexRequest = RandomCreateIndexGenerator.randomCreateIndexRequest();

        boolean humanReadable = randomBoolean();
        final XContentType xContentType = randomFrom(XContentType.values());
        BytesReference originalBytes = toShuffledXContent(createIndexRequest, xContentType, EMPTY_PARAMS, humanReadable);

        CreateIndexRequest parsedCreateIndexRequest = new CreateIndexRequest();
        parsedCreateIndexRequest.source(originalBytes, xContentType);

        assertMappingsEqual(createIndexRequest.mappings(), parsedCreateIndexRequest.mappings());
        assertAliasesEqual(createIndexRequest.aliases(), parsedCreateIndexRequest.aliases());
        assertEquals(createIndexRequest.settings(), parsedCreateIndexRequest.settings());

        BytesReference finalBytes = toShuffledXContent(parsedCreateIndexRequest, xContentType, EMPTY_PARAMS, humanReadable);
        ElasticsearchAssertions.assertToXContentEquivalent(originalBytes, finalBytes, xContentType);
    }

    public void testSettingsType() throws IOException {
        XContentBuilder builder = XContentFactory.contentBuilder(randomFrom(XContentType.values()));
        builder.startObject().startArray("settings").endArray().endObject();

        CreateIndexRequest parsedCreateIndexRequest = new CreateIndexRequest();
        ElasticsearchParseException e = expectThrows(ElasticsearchParseException.class, () -> parsedCreateIndexRequest.source(builder));
        assertThat(e.getMessage(), equalTo("key [settings] must be an object"));
    }

    public static void assertMappingsEqual(Map<String, String> expected, Map<String, String> actual) throws IOException {
        assertEquals(expected.keySet(), actual.keySet());

        for (Map.Entry<String, String> expectedEntry : expected.entrySet()) {
            String expectedValue = expectedEntry.getValue();
            String actualValue = actual.get(expectedEntry.getKey());
            try (XContentParser expectedJson = JsonXContent.jsonXContent.createParser(NamedXContentRegistry.EMPTY,
                    LoggingDeprecationHandler.INSTANCE, expectedValue);
                 XContentParser actualJson = JsonXContent.jsonXContent.createParser(NamedXContentRegistry.EMPTY,
                    LoggingDeprecationHandler.INSTANCE, actualValue)){
                assertEquals(expectedJson.map(), actualJson.map());
            }
        }
    }

    public static void assertAliasesEqual(Set<Alias> expected, Set<Alias> actual) throws IOException {
        assertEquals(expected, actual);

        for (Alias expectedAlias : expected) {
            for (Alias actualAlias : actual) {
                if (expectedAlias.equals(actualAlias)) {
                    // As Alias#equals only looks at name, we check the equality of the other Alias parameters here.
                    assertEquals(expectedAlias.filter(), actualAlias.filter());
                    assertEquals(expectedAlias.indexRouting(), actualAlias.indexRouting());
                    assertEquals(expectedAlias.searchRouting(), actualAlias.searchRouting());
                }
            }
        }
    }
}
