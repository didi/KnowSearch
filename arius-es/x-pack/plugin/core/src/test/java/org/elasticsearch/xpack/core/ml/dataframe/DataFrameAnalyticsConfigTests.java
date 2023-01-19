/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.dataframe;

import com.carrotsearch.randomizedtesting.generators.CodepointSetGenerator;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.Version;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParseException;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.test.AbstractSerializingTestCase;
import org.elasticsearch.xpack.core.ml.dataframe.analyses.MlDataFrameAnalysisNamedXContentProvider;
import org.elasticsearch.xpack.core.ml.dataframe.analyses.OutlierDetectionTests;
import org.elasticsearch.xpack.core.ml.dataframe.analyses.Regression;
import org.elasticsearch.xpack.core.ml.utils.ToXContentParams;
import org.junit.Before;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

public class DataFrameAnalyticsConfigTests extends AbstractSerializingTestCase<DataFrameAnalyticsConfig> {

    @Override
    protected DataFrameAnalyticsConfig doParseInstance(XContentParser parser) throws IOException {
        ObjectParser<DataFrameAnalyticsConfig.Builder, Void> dataFrameAnalyticsConfigParser =
            lenient
                ? DataFrameAnalyticsConfig.LENIENT_PARSER
                : DataFrameAnalyticsConfig.STRICT_PARSER;
        return dataFrameAnalyticsConfigParser.apply(parser, null).build();
    }

    @Override
    protected NamedWriteableRegistry getNamedWriteableRegistry() {
        List<NamedWriteableRegistry.Entry> namedWriteables = new ArrayList<>();
        namedWriteables.addAll(new MlDataFrameAnalysisNamedXContentProvider().getNamedWriteables());
        namedWriteables.addAll(new SearchModule(Settings.EMPTY, false, Collections.emptyList()).getNamedWriteables());
        return new NamedWriteableRegistry(namedWriteables);
    }

    @Override
    protected NamedXContentRegistry xContentRegistry() {
        List<NamedXContentRegistry.Entry> namedXContent = new ArrayList<>();
        namedXContent.addAll(new MlDataFrameAnalysisNamedXContentProvider().getNamedXContentParsers());
        namedXContent.addAll(new SearchModule(Settings.EMPTY, false, Collections.emptyList()).getNamedXContents());
        return new NamedXContentRegistry(namedXContent);
    }

    @Override
    protected DataFrameAnalyticsConfig createTestInstance() {
        return createRandom(randomValidId(), lenient);
    }

    @Override
    protected Writeable.Reader<DataFrameAnalyticsConfig> instanceReader() {
        return DataFrameAnalyticsConfig::new;
    }

    public static DataFrameAnalyticsConfig createRandom(String id) {
        return createRandom(id, false);
    }

    public static DataFrameAnalyticsConfig createRandom(String id, boolean withGeneratedFields) {
        return createRandomBuilder(id, withGeneratedFields).build();
    }

    public static DataFrameAnalyticsConfig.Builder createRandomBuilder(String id) {
        return createRandomBuilder(id, false);
    }

    public static DataFrameAnalyticsConfig.Builder createRandomBuilder(String id, boolean withGeneratedFields) {
        DataFrameAnalyticsSource source = DataFrameAnalyticsSourceTests.createRandom();
        DataFrameAnalyticsDest dest = DataFrameAnalyticsDestTests.createRandom();
        DataFrameAnalyticsConfig.Builder builder = new DataFrameAnalyticsConfig.Builder()
            .setId(id)
            .setAnalysis(OutlierDetectionTests.createRandom())
            .setSource(source)
            .setDest(dest);
        if (randomBoolean()) {
            builder.setAnalyzedFields(new FetchSourceContext(true,
                generateRandomStringArray(10, 10, false, false),
                generateRandomStringArray(10, 10, false, false)));
        }
        if (randomBoolean()) {
            builder.setModelMemoryLimit(new ByteSizeValue(randomIntBetween(1, 16), randomFrom(ByteSizeUnit.MB, ByteSizeUnit.GB)));
        }
        if (randomBoolean()) {
            builder.setDescription(randomAlphaOfLength(20));
        }
        if (withGeneratedFields) {
            if (randomBoolean()) {
                builder.setCreateTime(Instant.now());
            }
            if (randomBoolean()) {
                builder.setVersion(Version.CURRENT);
            }
        }
        if (randomBoolean()) {
            builder.setAllowLazyStart(randomBoolean());
        }
        return builder;
    }

    public static String randomValidId() {
        CodepointSetGenerator generator = new CodepointSetGenerator("abcdefghijklmnopqrstuvwxyz".toCharArray());
        return generator.ofCodePointsLength(random(), 10, 10);
    }

    private static final String ANACHRONISTIC_QUERY_DATA_FRAME_ANALYTICS = "{\n" +
        "    \"id\": \"old-data-frame\",\n" +
        //query:match:type stopped being supported in 6.x
        "    \"source\": {\"index\":\"my-index\", \"query\": {\"match\" : {\"query\":\"fieldName\", \"type\": \"phrase\"}}},\n" +
        "    \"dest\": {\"index\":\"dest-index\"},\n" +
        "    \"analysis\": {\"outlier_detection\": {\"n_neighbors\": 10}}\n" +
        "}";

    private static final String MODERN_QUERY_DATA_FRAME_ANALYTICS = "{\n" +
        "    \"id\": \"data-frame\",\n" +
        // match_all if parsed, adds default values in the options
        "    \"source\": {\"index\":\"my-index\", \"query\": {\"match_all\" : {}}},\n" +
        "    \"dest\": {\"index\":\"dest-index\"},\n" +
        "    \"analysis\": {\"outlier_detection\": {\"n_neighbors\": 10}}\n" +
        "}";

    private boolean lenient;

    @Before
    public void chooseStrictOrLenient() {
        lenient = randomBoolean();
    }

    public void testQueryConfigStoresUserInputOnly() throws IOException {
        try (XContentParser parser = XContentFactory.xContent(XContentType.JSON)
            .createParser(xContentRegistry(),
                DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
                MODERN_QUERY_DATA_FRAME_ANALYTICS)) {

            DataFrameAnalyticsConfig config = DataFrameAnalyticsConfig.LENIENT_PARSER.apply(parser, null).build();
            assertThat(config.getSource().getQuery(), equalTo(Collections.singletonMap(MatchAllQueryBuilder.NAME, Collections.emptyMap())));
        }

        try (XContentParser parser = XContentFactory.xContent(XContentType.JSON)
            .createParser(xContentRegistry(),
                DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
                MODERN_QUERY_DATA_FRAME_ANALYTICS)) {

            DataFrameAnalyticsConfig config = DataFrameAnalyticsConfig.STRICT_PARSER.apply(parser, null).build();
            assertThat(config.getSource().getQuery(), equalTo(Collections.singletonMap(MatchAllQueryBuilder.NAME, Collections.emptyMap())));
        }
    }

    public void testPastQueryConfigParse() throws IOException {
        try (XContentParser parser = XContentFactory.xContent(XContentType.JSON)
            .createParser(xContentRegistry(),
                DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
                ANACHRONISTIC_QUERY_DATA_FRAME_ANALYTICS)) {

            DataFrameAnalyticsConfig config = DataFrameAnalyticsConfig.LENIENT_PARSER.apply(parser, null).build();
            ElasticsearchException e = expectThrows(ElasticsearchException.class, () -> config.getSource().getParsedQuery());
            assertEquals("[match] query doesn't support multiple fields, found [query] and [type]", e.getMessage());
        }

        try (XContentParser parser = XContentFactory.xContent(XContentType.JSON)
            .createParser(xContentRegistry(),
                DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
                ANACHRONISTIC_QUERY_DATA_FRAME_ANALYTICS)) {

            XContentParseException e = expectThrows(XContentParseException.class,
                () -> DataFrameAnalyticsConfig.STRICT_PARSER.apply(parser, null).build());
            assertThat(e.getMessage(), containsString("[data_frame_analytics_config] failed to parse field [source]"));
        }
    }

    public void testToXContentForInternalStorage() throws IOException {
        DataFrameAnalyticsConfig.Builder builder = createRandomBuilder("foo");

        // headers are only persisted to cluster state
        Map<String, String> headers = new HashMap<>();
        headers.put("header-name", "header-value");
        builder.setHeaders(headers);
        DataFrameAnalyticsConfig config = builder.build();

        ToXContent.MapParams params = new ToXContent.MapParams(Collections.singletonMap(ToXContentParams.FOR_INTERNAL_STORAGE, "true"));

        BytesReference forClusterstateXContent = XContentHelper.toXContent(config, XContentType.JSON, params, false);
        XContentParser parser = XContentFactory.xContent(XContentType.JSON)
            .createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, forClusterstateXContent.streamInput());

        DataFrameAnalyticsConfig parsedConfig = DataFrameAnalyticsConfig.LENIENT_PARSER.apply(parser, null).build();
        assertThat(parsedConfig.getHeaders(), hasEntry("header-name", "header-value"));

        // headers are not written without the FOR_INTERNAL_STORAGE param
        BytesReference nonClusterstateXContent = XContentHelper.toXContent(config, XContentType.JSON, ToXContent.EMPTY_PARAMS, false);
        parser = XContentFactory.xContent(XContentType.JSON)
            .createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, nonClusterstateXContent.streamInput());

        parsedConfig = DataFrameAnalyticsConfig.LENIENT_PARSER.apply(parser, null).build();
        assertThat(parsedConfig.getHeaders().entrySet(), hasSize(0));
    }

    public void testInvalidModelMemoryLimits() {

        DataFrameAnalyticsConfig.Builder builder = new DataFrameAnalyticsConfig.Builder();

        // All these are different ways of specifying a limit that is lower than the minimum
        assertTooSmall(expectThrows(ElasticsearchStatusException.class,
            () -> builder.setModelMemoryLimit(new ByteSizeValue(-1, ByteSizeUnit.BYTES)).build()));
        assertTooSmall(expectThrows(ElasticsearchStatusException.class,
            () -> builder.setModelMemoryLimit(new ByteSizeValue(0, ByteSizeUnit.BYTES)).build()));
        assertTooSmall(expectThrows(ElasticsearchStatusException.class,
            () -> builder.setModelMemoryLimit(new ByteSizeValue(0, ByteSizeUnit.KB)).build()));
        assertTooSmall(expectThrows(ElasticsearchStatusException.class,
            () -> builder.setModelMemoryLimit(new ByteSizeValue(0, ByteSizeUnit.MB)).build()));
        assertTooSmall(expectThrows(ElasticsearchStatusException.class,
            () -> builder.setModelMemoryLimit(new ByteSizeValue(1023, ByteSizeUnit.BYTES)).build()));
    }

    public void testNoMemoryCapping() {

        DataFrameAnalyticsConfig uncapped = createRandom("foo");

        ByteSizeValue unlimited = randomBoolean() ? null : ByteSizeValue.ZERO;
        assertThat(uncapped.getModelMemoryLimit(),
            equalTo(new DataFrameAnalyticsConfig.Builder(uncapped, unlimited).build().getModelMemoryLimit()));
    }

    public void testMemoryCapping() {

        DataFrameAnalyticsConfig defaultLimitConfig = createRandomBuilder("foo").setModelMemoryLimit(null).build();

        ByteSizeValue maxLimit = new ByteSizeValue(randomIntBetween(500, 1000), ByteSizeUnit.MB);
        if (maxLimit.compareTo(defaultLimitConfig.getModelMemoryLimit()) < 0) {
            assertThat(maxLimit,
                equalTo(new DataFrameAnalyticsConfig.Builder(defaultLimitConfig, maxLimit).build().getModelMemoryLimit()));
        } else {
            assertThat(defaultLimitConfig.getModelMemoryLimit(),
                equalTo(new DataFrameAnalyticsConfig.Builder(defaultLimitConfig, maxLimit).build().getModelMemoryLimit()));
        }
    }

    public void testExplicitModelMemoryLimitTooHigh() {

        ByteSizeValue configuredLimit = new ByteSizeValue(randomIntBetween(5, 10), ByteSizeUnit.GB);
        DataFrameAnalyticsConfig explicitLimitConfig = createRandomBuilder("foo").setModelMemoryLimit(configuredLimit).build();

        ByteSizeValue maxLimit = new ByteSizeValue(randomIntBetween(500, 1000), ByteSizeUnit.MB);
        ElasticsearchStatusException e = expectThrows(ElasticsearchStatusException.class,
            () -> new DataFrameAnalyticsConfig.Builder(explicitLimitConfig, maxLimit).build());
        assertThat(e.getMessage(), startsWith("model_memory_limit"));
        assertThat(e.getMessage(), containsString("must be less than the value of the xpack.ml.max_model_memory_limit setting"));
    }

    public void testBuildForExplain() {
        DataFrameAnalyticsConfig.Builder builder = createRandomBuilder("foo");

        DataFrameAnalyticsConfig config = builder.buildForExplain();

        assertThat(config, equalTo(builder.build()));
    }

    public void testBuildForExplain_MissingId() {
        DataFrameAnalyticsConfig.Builder builder = new DataFrameAnalyticsConfig.Builder()
            .setAnalysis(OutlierDetectionTests.createRandom())
            .setSource(DataFrameAnalyticsSourceTests.createRandom())
            .setDest(DataFrameAnalyticsDestTests.createRandom());

        DataFrameAnalyticsConfig config = builder.buildForExplain();

        assertThat(config.getId(), equalTo("dummy"));
    }

    public void testBuildForExplain_MissingDest() {
        DataFrameAnalyticsConfig.Builder builder = new DataFrameAnalyticsConfig.Builder()
            .setId("foo")
            .setAnalysis(OutlierDetectionTests.createRandom())
            .setSource(DataFrameAnalyticsSourceTests.createRandom());

        DataFrameAnalyticsConfig config = builder.buildForExplain();

        assertThat(config.getDest().getIndex(), equalTo("dummy"));
    }

    public void testPreventCreateTimeInjection() throws IOException {
        String json = "{"
            + " \"create_time\" : 123456789 },"
            + " \"source\" : {\"index\":\"src\"},"
            + " \"dest\" : {\"index\": \"dest\"},"
            + "}";

        try (XContentParser parser =
                 XContentFactory.xContent(XContentType.JSON).createParser(
                     xContentRegistry(), DeprecationHandler.THROW_UNSUPPORTED_OPERATION, json)) {
            Exception e = expectThrows(IllegalArgumentException.class, () -> DataFrameAnalyticsConfig.STRICT_PARSER.apply(parser, null));
            assertThat(e.getMessage(), containsString("unknown field [create_time]"));
        }
    }

    public void testPreventVersionInjection() throws IOException {
        String json = "{"
            + " \"version\" : \"7.3.0\","
            + " \"source\" : {\"index\":\"src\"},"
            + " \"dest\" : {\"index\": \"dest\"},"
            + "}";

        try (XContentParser parser =
                 XContentFactory.xContent(XContentType.JSON).createParser(
                     xContentRegistry(), DeprecationHandler.THROW_UNSUPPORTED_OPERATION, json)) {
            Exception e = expectThrows(IllegalArgumentException.class, () -> DataFrameAnalyticsConfig.STRICT_PARSER.apply(parser, null));
            assertThat(e.getMessage(), containsString("unknown field [version]"));
        }
    }

    public void testToXContent_GivenAnalysisWithRandomizeSeedAndVersionIsCurrent() throws IOException {
        Regression regression = new Regression("foo");
        assertThat(regression.getRandomizeSeed(), is(notNullValue()));

        DataFrameAnalyticsConfig config = new DataFrameAnalyticsConfig.Builder()
            .setVersion(Version.CURRENT)
            .setId("test_config")
            .setSource(new DataFrameAnalyticsSource(new String[] {"source_index"}, null, null))
            .setDest(new DataFrameAnalyticsDest("dest_index", null))
            .setAnalysis(regression)
            .build();

        try (XContentBuilder builder = JsonXContent.contentBuilder()) {
            config.toXContent(builder, ToXContent.EMPTY_PARAMS);
            String json = Strings.toString(builder);
            assertThat(json, containsString("randomize_seed"));
        }
    }

    public void testToXContent_GivenAnalysisWithRandomizeSeedAndVersionIsBeforeItWasIntroduced() throws IOException {
        Regression regression = new Regression("foo");
        assertThat(regression.getRandomizeSeed(), is(notNullValue()));

        DataFrameAnalyticsConfig config = new DataFrameAnalyticsConfig.Builder()
            .setVersion(Version.V_7_5_0)
            .setId("test_config")
            .setSource(new DataFrameAnalyticsSource(new String[] {"source_index"}, null, null))
            .setDest(new DataFrameAnalyticsDest("dest_index", null))
            .setAnalysis(regression)
            .build();

        try (XContentBuilder builder = JsonXContent.contentBuilder()) {
            config.toXContent(builder, ToXContent.EMPTY_PARAMS);
            String json = Strings.toString(builder);
            assertThat(json, not(containsString("randomize_seed")));
        }
    }

    public void testExtractJobIdFromDocId() {
        assertThat(DataFrameAnalyticsConfig.extractJobIdFromDocId("data_frame_analytics_config-foo"), equalTo("foo"));
        assertThat(DataFrameAnalyticsConfig.extractJobIdFromDocId("data_frame_analytics_config-data_frame_analytics_config-foo"),
            equalTo("data_frame_analytics_config-foo"));
        assertThat(DataFrameAnalyticsConfig.extractJobIdFromDocId("foo"), is(nullValue()));
    }

    private static void assertTooSmall(ElasticsearchStatusException e) {
        assertThat(e.getMessage(), startsWith("model_memory_limit must be at least 1kb."));
    }
}
