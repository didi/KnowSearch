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

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.PointInSetQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.indices.TermsLookup;
import org.elasticsearch.test.AbstractQueryTestCase;
import org.hamcrest.CoreMatchers;
import org.junit.Before;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.instanceOf;

public class TermsQueryBuilderTests extends AbstractQueryTestCase<TermsQueryBuilder> {
    private List<Object> randomTerms;
    private String termsPath;

    @Before
    public void randomTerms() {
        List<Object> randomTerms = new ArrayList<>();
        String[] strings = generateRandomStringArray(10, 10, false, true);
        for (String string : strings) {
            randomTerms.add(string);
            if (rarely()) {
                randomTerms.add(null);
            }
        }
        this.randomTerms = randomTerms;
        termsPath = randomAlphaOfLength(10).replace('.', '_');
    }

    @Override
    protected TermsQueryBuilder doCreateTestQueryBuilder() {
        TermsQueryBuilder query;
        // terms query or lookup query
        if (randomBoolean()) {
            // make between 0 and 5 different values of the same type
            String fieldName = randomValueOtherThanMany(choice ->
                    choice.equals(GEO_POINT_FIELD_NAME) ||
                    choice.equals(GEO_POINT_ALIAS_FIELD_NAME) ||
                    choice.equals(GEO_SHAPE_FIELD_NAME) ||
                    choice.equals(INT_RANGE_FIELD_NAME) ||
                    choice.equals(DATE_RANGE_FIELD_NAME) ||
                    choice.equals(DATE_NANOS_FIELD_NAME), // TODO: needs testing for date_nanos type
                () -> getRandomFieldName());
            Object[] values = new Object[randomInt(5)];
            for (int i = 0; i < values.length; i++) {
                values[i] = getRandomValueForFieldName(fieldName);
            }
            query = new TermsQueryBuilder(fieldName, values);
        } else {
            // right now the mock service returns us a list of strings
            query = new TermsQueryBuilder(randomBoolean() ? randomAlphaOfLengthBetween(1,10) : STRING_FIELD_NAME, randomTermsLookup());
        }
        return query;
    }

    private TermsLookup randomTermsLookup() {
        // Randomly choose between a typeless terms lookup and one with an explicit type to make sure we are
        // testing both cases.
        TermsLookup lookup = randomBoolean()
            ? new TermsLookup(randomAlphaOfLength(10), randomAlphaOfLength(10), termsPath)
            : new TermsLookup(randomAlphaOfLength(10), randomAlphaOfLength(10), randomAlphaOfLength(10), termsPath);
        lookup.routing(randomBoolean() ? randomAlphaOfLength(10) : null);
        return lookup;
    }

    @Override
    protected void doAssertLuceneQuery(TermsQueryBuilder queryBuilder, Query query, QueryShardContext context) throws IOException {
        if (queryBuilder.termsLookup() == null && (queryBuilder.values() == null || queryBuilder.values().isEmpty())) {
            assertThat(query, instanceOf(MatchNoDocsQuery.class));
            MatchNoDocsQuery matchNoDocsQuery = (MatchNoDocsQuery) query;
            assertThat(matchNoDocsQuery.toString(), containsString("No terms supplied for \"terms\" query."));
        } else if (queryBuilder.termsLookup() != null && randomTerms.size() == 0){
            assertThat(query, instanceOf(MatchNoDocsQuery.class));
            MatchNoDocsQuery matchNoDocsQuery = (MatchNoDocsQuery) query;
            assertThat(matchNoDocsQuery.toString(), containsString("No terms supplied for \"terms\" query."));
        } else {
            assertThat(query, either(instanceOf(TermInSetQuery.class))
                    .or(instanceOf(PointInSetQuery.class))
                    .or(instanceOf(ConstantScoreQuery.class)));
            if (query instanceof ConstantScoreQuery) {
                assertThat(((ConstantScoreQuery) query).getQuery(), instanceOf(BooleanQuery.class));
            }

            // we only do the check below for string fields (otherwise we'd have to decode the values)
            if (queryBuilder.fieldName().equals(INT_FIELD_NAME) || queryBuilder.fieldName().equals(DOUBLE_FIELD_NAME)
                    || queryBuilder.fieldName().equals(BOOLEAN_FIELD_NAME) || queryBuilder.fieldName().equals(DATE_FIELD_NAME)) {
                return;
            }

            // expected returned terms depending on whether we have a terms query or a terms lookup query
            List<Object> terms;
            if (queryBuilder.termsLookup() != null) {
                terms = randomTerms;
            } else {
                terms = queryBuilder.values();
            }

            String fieldName = expectedFieldName(queryBuilder.fieldName());
            TermInSetQuery expected = new TermInSetQuery(fieldName,
                    terms.stream().filter(Objects::nonNull).map(Object::toString).map(BytesRef::new).collect(Collectors.toList()));
            assertEquals(expected, query);
        }
    }

    public void testEmtpyFieldName() {
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, () -> new TermsQueryBuilder(null, "term"));
        assertEquals("field name cannot be null.", e.getMessage());
        e = expectThrows(IllegalArgumentException.class, () -> new TermsQueryBuilder("", "term"));
        assertEquals("field name cannot be null.", e.getMessage());
    }

    public void testEmtpyTermsLookup() {
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, () -> new TermsQueryBuilder("field", (TermsLookup) null));
        assertEquals("No value or termsLookup specified for terms query", e.getMessage());
    }

    public void testNullValues() {
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, () -> new TermsQueryBuilder("field", (String[]) null));
        assertThat(e.getMessage(), containsString("No value specified for terms query"));
        e = expectThrows(IllegalArgumentException.class, () -> new TermsQueryBuilder("field", (int[]) null));
        assertThat(e.getMessage(), containsString("No value specified for terms query"));
        e = expectThrows(IllegalArgumentException.class, () -> new TermsQueryBuilder("field", (long[]) null));
        assertThat(e.getMessage(), containsString("No value specified for terms query"));
        e = expectThrows(IllegalArgumentException.class, () -> new TermsQueryBuilder("field", (float[]) null));
        assertThat(e.getMessage(), containsString("No value specified for terms query"));
        e = expectThrows(IllegalArgumentException.class, () -> new TermsQueryBuilder("field", (double[]) null));
        assertThat(e.getMessage(), containsString("No value specified for terms query"));
        e = expectThrows(IllegalArgumentException.class, () -> new TermsQueryBuilder("field", (Object[]) null));
        assertThat(e.getMessage(), containsString("No value specified for terms query"));
        e = expectThrows(IllegalArgumentException.class, () -> new TermsQueryBuilder("field", (Iterable<?>) null));
        assertThat(e.getMessage(), containsString("No value specified for terms query"));
    }

    public void testBothValuesAndLookupSet() throws IOException {
        String query = "{\n" +
                "  \"terms\": {\n" +
                "    \"field\": [\n" +
                "      \"blue\",\n" +
                "      \"pill\"\n" +
                "    ],\n" +
                "    \"field_lookup\": {\n" +
                "      \"index\": \"pills\",\n" +
                "      \"type\": \"red\",\n" +
                "      \"id\": \"3\",\n" +
                "      \"path\": \"white rabbit\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        ParsingException e = expectThrows(ParsingException.class, () -> parseQuery(query));
        assertThat(e.getMessage(), containsString("[" + TermsQueryBuilder.NAME + "] query does not support more than one field."));
    }

    @Override
    public GetResponse executeGet(GetRequest getRequest) {
        String json;
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().prettyPrint();
            builder.startObject();
            builder.array(termsPath, randomTerms.toArray(new Object[randomTerms.size()]));
            builder.endObject();
            json = Strings.toString(builder);
        } catch (IOException ex) {
            throw new ElasticsearchException("boom", ex);
        }
        return new GetResponse(new GetResult(getRequest.index(), getRequest.type(), getRequest.id(), 0, 1, 0, true,
            new BytesArray(json), null, null));
    }

    public void testNumeric() throws IOException {
        {
            TermsQueryBuilder builder = new TermsQueryBuilder("foo", new int[]{1, 3, 4});
            TermsQueryBuilder copy = (TermsQueryBuilder) assertSerialization(builder);
            List<Object> values = copy.values();
            assertEquals(Arrays.asList(1L, 3L, 4L), values);
        }
        {
            TermsQueryBuilder builder = new TermsQueryBuilder("foo", new double[]{1, 3, 4});
            TermsQueryBuilder copy = (TermsQueryBuilder) assertSerialization(builder);
            List<Object> values = copy.values();
            assertEquals(Arrays.asList(1d, 3d, 4d), values);
        }
        {
            TermsQueryBuilder builder = new TermsQueryBuilder("foo", new float[]{1, 3, 4});
            TermsQueryBuilder copy = (TermsQueryBuilder) assertSerialization(builder);
            List<Object> values = copy.values();
            assertEquals(Arrays.asList(1f, 3f, 4f), values);
        }
        {
            TermsQueryBuilder builder = new TermsQueryBuilder("foo", new long[]{1, 3, 4});
            TermsQueryBuilder copy = (TermsQueryBuilder) assertSerialization(builder);
            List<Object> values = copy.values();
            assertEquals(Arrays.asList(1L, 3L, 4L), values);
        }
    }

    public void testTermsQueryWithMultipleFields() throws IOException {
        String query = Strings.toString(XContentFactory.jsonBuilder().startObject()
                .startObject("terms").array("foo", 123).array("bar", 456).endObject()
                .endObject());
        ParsingException e = expectThrows(ParsingException.class, () -> parseQuery(query));
        assertEquals("[" + TermsQueryBuilder.NAME + "] query does not support multiple fields", e.getMessage());
    }

    public void testFromJson() throws IOException {
        String json =
                "{\n" +
                "  \"terms\" : {\n" +
                "    \"user\" : [ \"kimchy\", \"elasticsearch\" ],\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}";

        TermsQueryBuilder parsed = (TermsQueryBuilder) parseQuery(json);
        checkGeneratedJson(json, parsed);
        assertEquals(json, 2, parsed.values().size());
    }

    @Override
    public void testMustRewrite() throws IOException {
        TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder(STRING_FIELD_NAME, randomTermsLookup());
        UnsupportedOperationException e = expectThrows(UnsupportedOperationException.class,
                () -> termsQueryBuilder.toQuery(createShardContext()));
        assertEquals("query must be rewritten first", e.getMessage());
        assertEquals(rewriteAndFetch(termsQueryBuilder, createShardContext()), new TermsQueryBuilder(STRING_FIELD_NAME,
            randomTerms.stream().filter(x -> x != null).collect(Collectors.toList()))); // terms lookup removes null values
    }

    public void testGeo() throws Exception {
        TermsQueryBuilder query = new TermsQueryBuilder(GEO_POINT_FIELD_NAME, "2,3");
        QueryShardContext context = createShardContext();
        QueryShardException e = expectThrows(QueryShardException.class,
                () -> query.toQuery(context));
        assertEquals("Geo fields do not support exact searching, use dedicated geo queries instead: [mapped_geo_point]",
                e.getMessage());
    }

    public void testSerializationFailsUnlessFetched() throws IOException {
        QueryBuilder builder = new TermsQueryBuilder(STRING_FIELD_NAME, randomTermsLookup());
        QueryBuilder termsQueryBuilder = Rewriteable.rewrite(builder, createShardContext());
        IllegalStateException ise = expectThrows(IllegalStateException.class, () -> termsQueryBuilder.writeTo(new BytesStreamOutput(10)));
        assertEquals(ise.getMessage(), "supplier must be null, can't serialize suppliers, missing a rewriteAndFetch?");
        builder = rewriteAndFetch(builder, createShardContext());
        builder.writeTo(new BytesStreamOutput(10));
    }

    public void testConversion() {
        List<Object> list = Arrays.asList();
        assertSame(Collections.emptyList(), TermsQueryBuilder.convert(list));
        assertEquals(list, TermsQueryBuilder.convertBack(TermsQueryBuilder.convert(list)));

        list = Arrays.asList("abc");
        assertEquals(Arrays.asList(new BytesRef("abc")), TermsQueryBuilder.convert(list));
        assertEquals(list, TermsQueryBuilder.convertBack(TermsQueryBuilder.convert(list)));

        list = Arrays.asList("abc", new BytesRef("def"));
        assertEquals(Arrays.asList(new BytesRef("abc"), new BytesRef("def")), TermsQueryBuilder.convert(list));
        assertEquals(Arrays.asList("abc", "def"), TermsQueryBuilder.convertBack(TermsQueryBuilder.convert(list)));

        list = Arrays.asList(5, 42L);
        assertEquals(Arrays.asList(5L, 42L), TermsQueryBuilder.convert(list));
        assertEquals(Arrays.asList(5L, 42L), TermsQueryBuilder.convertBack(TermsQueryBuilder.convert(list)));

        list = Arrays.asList(5, 42d);
        assertEquals(Arrays.asList(5, 42d), TermsQueryBuilder.convert(list));
        assertEquals(Arrays.asList(5, 42d), TermsQueryBuilder.convertBack(TermsQueryBuilder.convert(list)));
    }

    public void testTypeField() throws IOException {
        TermsQueryBuilder builder = QueryBuilders.termsQuery("_type", "value1", "value2");
        builder.doToQuery(createShardContext());
        assertWarnings(QueryShardContext.TYPES_DEPRECATION_MESSAGE);
    }
    
    public void testRewriteIndexQueryToMatchNone() throws IOException {
        TermsQueryBuilder query = new TermsQueryBuilder("_index", "does_not_exist", "also_does_not_exist");
        QueryShardContext queryShardContext = createShardContext();
        QueryBuilder rewritten = query.rewrite(queryShardContext);
        assertThat(rewritten, instanceOf(MatchNoneQueryBuilder.class));
    }      
    
    public void testRewriteIndexQueryToNotMatchNone() throws IOException {
        // At least one name is good
        TermsQueryBuilder query = new TermsQueryBuilder("_index", "does_not_exist", getIndex().getName());
        QueryShardContext queryShardContext = createShardContext();
        QueryBuilder rewritten = query.rewrite(queryShardContext);
        assertThat(rewritten, instanceOf(TermsQueryBuilder.class));
    }      
    
    @Override
    protected QueryBuilder parseQuery(XContentParser parser) throws IOException {
        QueryBuilder query = super.parseQuery(parser);
        assertThat(query, CoreMatchers.instanceOf(TermsQueryBuilder.class));

        TermsQueryBuilder termsQuery = (TermsQueryBuilder) query;
        if (termsQuery.isTypeless() == false) {
            assertWarnings(TermsQueryBuilder.TYPES_DEPRECATION_MESSAGE);
        }
        return query;
    }
}
