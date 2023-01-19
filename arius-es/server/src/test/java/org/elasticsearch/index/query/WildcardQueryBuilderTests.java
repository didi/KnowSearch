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

package org.elasticsearch.index.query;

import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.test.AbstractQueryTestCase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

public class WildcardQueryBuilderTests extends AbstractQueryTestCase<WildcardQueryBuilder> {

    @Override
    protected WildcardQueryBuilder doCreateTestQueryBuilder() {
        WildcardQueryBuilder query = randomWildcardQuery();
        if (randomBoolean()) {
            query.rewrite(randomFrom(getRandomRewriteMethod()));
        }
        return query;
    }

    @Override
    protected Map<String, WildcardQueryBuilder> getAlternateVersions() {
        Map<String, WildcardQueryBuilder> alternateVersions = new HashMap<>();
        WildcardQueryBuilder wildcardQuery = randomWildcardQuery();
        String contentString = "{\n" +
                "    \"wildcard\" : {\n" +
                "        \"" + wildcardQuery.fieldName() + "\" : \"" + wildcardQuery.value() + "\"\n" +
                "    }\n" +
                "}";
        alternateVersions.put(contentString, wildcardQuery);
        return alternateVersions;
    }

    private static WildcardQueryBuilder randomWildcardQuery() {
        String fieldName = randomFrom(STRING_FIELD_NAME,
            STRING_ALIAS_FIELD_NAME,
            randomAlphaOfLengthBetween(1, 10));
        String text = randomAlphaOfLengthBetween(1, 10);

        return new WildcardQueryBuilder(fieldName, text);
    }

    @Override
    protected void doAssertLuceneQuery(WildcardQueryBuilder queryBuilder, Query query, QueryShardContext context) throws IOException {
        String expectedFieldName = expectedFieldName(queryBuilder.fieldName());

        if (expectedFieldName.equals(STRING_FIELD_NAME)) {
            assertThat(query, instanceOf(WildcardQuery.class));
            WildcardQuery wildcardQuery = (WildcardQuery) query;

            assertThat(wildcardQuery.getField(), equalTo(expectedFieldName));
            assertThat(wildcardQuery.getTerm().field(), equalTo(expectedFieldName));
            assertThat(wildcardQuery.getTerm().text(), equalTo(queryBuilder.value()));
        } else {
            Query expected = new MatchNoDocsQuery("unknown field [" + expectedFieldName + "]");
            assertEquals(expected, query);
        }
    }

    public void testIllegalArguments() {
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, () -> new WildcardQueryBuilder(null, "text"));
        assertEquals("field name is null or empty", e.getMessage());
        e = expectThrows(IllegalArgumentException.class, () -> new WildcardQueryBuilder("", "text"));
        assertEquals("field name is null or empty", e.getMessage());

        e = expectThrows(IllegalArgumentException.class, () -> new WildcardQueryBuilder("field", null));
        assertEquals("value cannot be null", e.getMessage());
    }

    public void testEmptyValue() throws IOException {
        QueryShardContext context = createShardContext();
        context.setAllowUnmappedFields(true);
        WildcardQueryBuilder wildcardQueryBuilder = new WildcardQueryBuilder(STRING_FIELD_NAME, "");
        assertEquals(wildcardQueryBuilder.toQuery(context).getClass(), WildcardQuery.class);
    }

    public void testFromJson() throws IOException {
        String json = "{    \"wildcard\" : { \"user\" : { \"wildcard\" : \"ki*y\", \"boost\" : 2.0 } }}";
        WildcardQueryBuilder parsed = (WildcardQueryBuilder) parseQuery(json);
        checkGeneratedJson(json, parsed);
        assertEquals(json, "ki*y", parsed.value());
        assertEquals(json, 2.0, parsed.boost(), 0.0001);
    }

    public void testParseFailsWithMultipleFields() throws IOException {
        String json =
                "{\n" +
                "    \"wildcard\": {\n" +
                "      \"user1\": {\n" +
                "        \"wildcard\": \"ki*y\"\n" +
                "      },\n" +
                "      \"user2\": {\n" +
                "        \"wildcard\": \"ki*y\"\n" +
                "      }\n" +
                "    }\n" +
                "}";
        ParsingException e = expectThrows(ParsingException.class, () -> parseQuery(json));
        assertEquals("[wildcard] query doesn't support multiple fields, found [user1] and [user2]", e.getMessage());

        String shortJson =
                "{\n" +
                "    \"wildcard\": {\n" +
                "      \"user1\": \"ki*y\",\n" +
                "      \"user2\": \"ki*y\"\n" +
                "    }\n" +
                "}";
        e = expectThrows(ParsingException.class, () -> parseQuery(shortJson));
        assertEquals("[wildcard] query doesn't support multiple fields, found [user1] and [user2]", e.getMessage());
    }

    public void testTypeField() throws IOException {
        WildcardQueryBuilder builder = QueryBuilders.wildcardQuery("_type", "doc*");
        builder.doToQuery(createShardContext());
        assertWarnings(QueryShardContext.TYPES_DEPRECATION_MESSAGE);
    }
    
    public void testRewriteIndexQueryToMatchNone() throws IOException {
        WildcardQueryBuilder query = new WildcardQueryBuilder("_index", "does_not_exist");
        QueryShardContext queryShardContext = createShardContext();
        QueryBuilder rewritten = query.rewrite(queryShardContext);
        assertThat(rewritten, instanceOf(MatchNoneQueryBuilder.class));
    }   
    
    public void testRewriteIndexQueryNotMatchNone() throws IOException {
        String fullIndexName = getIndex().getName();
        String firstHalfOfIndexName = fullIndexName.substring(0,fullIndexName.length()/2);
        WildcardQueryBuilder query = new WildcardQueryBuilder("_index", firstHalfOfIndexName +"*");
        QueryShardContext queryShardContext = createShardContext();
        QueryBuilder rewritten = query.rewrite(queryShardContext);
        assertThat(rewritten, instanceOf(WildcardQueryBuilder.class));
    }      
}
