/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.deprecation;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.joda.JodaDeprecationPatterns;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.mapper.FieldNamesFieldMapper;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.VersionUtils;
import org.elasticsearch.xpack.core.deprecation.DeprecationIssue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.singletonList;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.xpack.deprecation.DeprecationChecks.INDEX_SETTINGS_CHECKS;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

public class IndexDeprecationChecksTests extends ESTestCase {
    public void testOldIndicesCheck() {
        Version createdWith = VersionUtils.randomVersionBetween(random(), Version.V_6_0_0,
            VersionUtils.getPreviousVersion(Version.V_7_0_0));
        IndexMetaData indexMetaData = IndexMetaData.builder("test")
            .settings(settings(createdWith))
            .numberOfShards(1)
            .numberOfReplicas(0)
            .build();
        DeprecationIssue expected = new DeprecationIssue(DeprecationIssue.Level.CRITICAL,
            "Index created before 7.0",
            "https://www.elastic.co/guide/en/elasticsearch/reference/master/" +
                "breaking-changes-8.0.html",
            "This index was created using version: " + createdWith);
        List<DeprecationIssue> issues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(indexMetaData));
        assertEquals(singletonList(expected), issues);
    }

    public void testTooManyFieldsCheck() throws IOException {
        String simpleMapping = "{\n" +
            "  \"properties\": {\n" +
            "    \"some_field\": {\n" +
            "      \"type\": \"text\"\n" +
            "    },\n" +
            "    \"other_field\": {\n" +
            "      \"type\": \"text\",\n" +
            "      \"properties\": {\n" +
            "        \"raw\": {\"type\": \"keyword\"}\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        IndexMetaData simpleIndex = IndexMetaData.builder(randomAlphaOfLengthBetween(5, 10))
            .settings(settings(Version.V_7_0_0))
            .numberOfShards(randomIntBetween(1, 100))
            .numberOfReplicas(randomIntBetween(1, 100))
            .putMapping("_doc", simpleMapping)
            .build();
        List<DeprecationIssue> noIssues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(simpleIndex));
        assertEquals(0, noIssues.size());

        // Test that it catches having too many fields
        int fieldCount = randomIntBetween(1025, 10_000); // 10_000 is arbitrary

        XContentBuilder mappingBuilder = jsonBuilder();
        mappingBuilder.startObject();
        {
            mappingBuilder.startObject("properties");
            {
                addRandomFields(fieldCount, mappingBuilder);
            }
            mappingBuilder.endObject();
        }
        mappingBuilder.endObject();

        IndexMetaData tooManyFieldsIndex = IndexMetaData.builder(randomAlphaOfLengthBetween(5, 10))
            .settings(settings(Version.V_7_0_0))
            .numberOfShards(randomIntBetween(1, 100))
            .numberOfReplicas(randomIntBetween(1, 100))
            .putMapping("_doc", Strings.toString(mappingBuilder))
            .build();
        DeprecationIssue expected = new DeprecationIssue(DeprecationIssue.Level.WARNING,
            "Number of fields exceeds automatic field expansion limit",
            "https://www.elastic.co/guide/en/elasticsearch/reference/7.0/breaking-changes-7.0.html" +
                "#_limiting_the_number_of_auto_expanded_fields",
            "This index has [" + fieldCount + "] fields, which exceeds the automatic field expansion limit of 1024 " +
                "and does not have [" + IndexSettings.DEFAULT_FIELD_SETTING.getKey() + "] set, which may cause queries which use " +
                "automatic field expansion, such as query_string, simple_query_string, and multi_match to fail if fields are not " +
                "explicitly specified in the query.");
        List<DeprecationIssue> issues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(tooManyFieldsIndex));
        assertEquals(singletonList(expected), issues);

        // Check that it's okay to  have too many fields as long as `index.query.default_field` is set
        IndexMetaData tooManyFieldsOk = IndexMetaData.builder(randomAlphaOfLengthBetween(5, 10))
            .settings(settings(Version.V_7_0_0)
                .put(IndexSettings.DEFAULT_FIELD_SETTING.getKey(), randomAlphaOfLength(5)))
            .numberOfShards(randomIntBetween(1, 100))
            .numberOfReplicas(randomIntBetween(1, 100))
            .putMapping("_doc", Strings.toString(mappingBuilder))
            .build();
        List<DeprecationIssue> withDefaultFieldIssues =
            DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(tooManyFieldsOk));
        assertEquals(0, withDefaultFieldIssues.size());
    }

    public void testChainedMultiFields() throws IOException {
        XContentBuilder xContent = XContentFactory.jsonBuilder().startObject()
            .startObject("properties")
                .startObject("invalid-field")
                    .field("type", "keyword")
                    .startObject("fields")
                        .startObject("sub-field")
                            .field("type", "keyword")
                            .startObject("fields")
                                .startObject("sub-sub-field")
                                    .field("type", "keyword")
                                .endObject()
                            .endObject()
                        .endObject()
                    .endObject()
                .endObject()
                .startObject("valid-field")
                    .field("type", "keyword")
                    .startObject("fields")
                        .startObject("sub-field")
                            .field("type", "keyword")
                        .endObject()
                    .endObject()
                .endObject()
            .endObject()
        .endObject();
        String mapping = BytesReference.bytes(xContent).utf8ToString();

        IndexMetaData simpleIndex = IndexMetaData.builder(randomAlphaOfLengthBetween(5, 10))
            .settings(settings(Version.V_7_3_0))
            .numberOfShards(1)
            .numberOfReplicas(1)
            .putMapping("_doc", mapping)
            .build();
        List<DeprecationIssue> issues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(simpleIndex));
        assertEquals(1, issues.size());

        DeprecationIssue expected = new DeprecationIssue(DeprecationIssue.Level.WARNING,
            "Multi-fields within multi-fields",
            "https://www.elastic.co/guide/en/elasticsearch/reference/master/breaking-changes-8.0.html" +
                "#_defining_multi_fields_within_multi_fields",
            "The names of fields that contain chained multi-fields: [[type: _doc, field: invalid-field]]");
        assertEquals(singletonList(expected), issues);
    }

    public void testDefinedPatternsDoNotWarn() throws IOException {
        String simpleMapping = "{\n" +
            "\"properties\" : {\n" +
            "   \"date_time_field_Y\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"strictWeekyearWeek\"\n" +
            "       }\n" +
            "   }" +
            "}";
        IndexMetaData simpleIndex = createV6Index(simpleMapping);

        DeprecationIssue issue = IndexDeprecationChecks.deprecatedDateTimeFormat(simpleIndex);
        assertNull(issue);
    }

    public void testMigratedPatterns() throws IOException {
        String simpleMapping = "{\n" +
            "\"properties\" : {\n" +
            "   \"date_time_field_Y\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"8MM-YYYY\"\n" +
            "       }\n" +
            "   }" +
            "}";
        IndexMetaData simpleIndex = createV6Index(simpleMapping);

        DeprecationIssue issue = IndexDeprecationChecks.deprecatedDateTimeFormat(simpleIndex);
        assertNull(issue);
    }

    public void testMultipleWarningsOnCombinedPattern() throws IOException {
        String simpleMapping = "{\n" +
            "\"properties\" : {\n" +
            "   \"date_time_field_Y\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"dd-CC||MM-YYYY\"\n" +
            "       }\n" +
            "   }" +
            "}";
        IndexMetaData simpleIndex = createV6Index(simpleMapping);

        DeprecationIssue expected = new DeprecationIssue(DeprecationIssue.Level.WARNING,
            "Date field format uses patterns which has changed meaning in 7.0",
            "https://www.elastic.co/guide/en/elasticsearch/reference/7.0/breaking-changes-7.0.html#breaking_70_java_time_changes",
            "This index has date fields with deprecated formats: ["+
                "[type: _doc, field: date_time_field_Y, format: dd-CC||MM-YYYY, " +
                "suggestion: 'C' century of era is no longer supported." +
                "; "+
                "'Y' year-of-era should be replaced with 'y'. Use 'Y' for week-based-year.]"+
                "]. "+ JodaDeprecationPatterns.USE_NEW_FORMAT_SPECIFIERS);
        List<DeprecationIssue> issues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(simpleIndex));
        assertThat(issues, hasItem(expected));
    }

    public void testDuplicateWarningsOnCombinedPattern() throws IOException {
        String simpleMapping = "{\n" +
            "\"properties\" : {\n" +
            "   \"date_time_field_Y\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"dd-YYYY||MM-YYYY\"\n" +
            "       }\n" +
            "   }" +
            "}";
        IndexMetaData simpleIndex = createV6Index(simpleMapping);

        DeprecationIssue expected = new DeprecationIssue(DeprecationIssue.Level.WARNING,
            "Date field format uses patterns which has changed meaning in 7.0",
            "https://www.elastic.co/guide/en/elasticsearch/reference/7.0/breaking-changes-7.0.html#breaking_70_java_time_changes",
            "This index has date fields with deprecated formats: ["+
                "[type: _doc, field: date_time_field_Y, format: dd-YYYY||MM-YYYY, " +
                "suggestion: 'Y' year-of-era should be replaced with 'y'. Use 'Y' for week-based-year.]"+
                "]. "+ JodaDeprecationPatterns.USE_NEW_FORMAT_SPECIFIERS);
        List<DeprecationIssue> issues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(simpleIndex));
        assertThat(issues, hasItem(expected));
    }

    public void testWarningsOnMixCustomAndDefinedPattern() throws IOException {
        String simpleMapping = "{\n" +
            "\"properties\" : {\n" +
            "   \"date_time_field_Y\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"strictWeekyearWeek||MM-YYYY\"\n" +
            "       }\n" +
            "   }" +
            "}";
        IndexMetaData simpleIndex = createV6Index(simpleMapping);

        DeprecationIssue expected = new DeprecationIssue(DeprecationIssue.Level.WARNING,
            "Date field format uses patterns which has changed meaning in 7.0",
            "https://www.elastic.co/guide/en/elasticsearch/reference/7.0/breaking-changes-7.0.html#breaking_70_java_time_changes",
            "This index has date fields with deprecated formats: ["+
                "[type: _doc, field: date_time_field_Y, format: strictWeekyearWeek||MM-YYYY, " +
                "suggestion: 'Y' year-of-era should be replaced with 'y'. Use 'Y' for week-based-year.]"+
                "]. "+ JodaDeprecationPatterns.USE_NEW_FORMAT_SPECIFIERS);
        List<DeprecationIssue> issues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(simpleIndex));
        assertThat(issues, hasItem(expected));
    }

    public void testJodaPatternDeprecations() throws IOException {
        String simpleMapping = "{\n" +
            "\"properties\" : {\n" +
            "   \"date_time_field_Y\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"MM-YYYY\"\n" +
            "       },\n" +
            "   \"date_time_field_C\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"CC\"\n" +
            "       },\n" +
            "   \"date_time_field_x\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"xx-MM\"\n" +
            "       },\n" +
            "   \"date_time_field_y\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"yy-MM\"\n" +
            "       },\n" +
            "   \"date_time_field_Z\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"HH:mmZ\"\n" +
            "       },\n" +
            "   \"date_time_field_z\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"HH:mmz\"\n" +
            "       }\n" +
            "   }" +
            "}";

        IndexMetaData simpleIndex = createV6Index(simpleMapping);

        DeprecationIssue expected = new DeprecationIssue(DeprecationIssue.Level.WARNING,
            "Date field format uses patterns which has changed meaning in 7.0",
            "https://www.elastic.co/guide/en/elasticsearch/reference/7.0/breaking-changes-7.0.html#breaking_70_java_time_changes",
            "This index has date fields with deprecated formats: ["+
                "[type: _doc, field: date_time_field_Y, format: MM-YYYY, " +
                "suggestion: 'Y' year-of-era should be replaced with 'y'. Use 'Y' for week-based-year.], "+
                "[type: _doc, field: date_time_field_C, format: CC, " +
                "suggestion: 'C' century of era is no longer supported.], "+
                "[type: _doc, field: date_time_field_x, format: xx-MM, " +
                "suggestion: 'x' weak-year should be replaced with 'Y'. Use 'x' for zone-offset.], "+
                "[type: _doc, field: date_time_field_y, format: yy-MM, " +
                "suggestion: 'y' year should be replaced with 'u'. Use 'y' for year-of-era.], "+
                "[type: _doc, field: date_time_field_Z, format: HH:mmZ, " +
                "suggestion: 'Z' time zone offset/id fails when parsing 'Z' for Zulu timezone. Consider using 'X'.], "+
                "[type: _doc, field: date_time_field_z, format: HH:mmz, " +
                "suggestion: 'z' time zone text. Will print 'Z' for Zulu given UTC timezone." +
                "]"+
                "]. "+ JodaDeprecationPatterns.USE_NEW_FORMAT_SPECIFIERS);
        List<DeprecationIssue> issues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(simpleIndex));
        assertThat(issues, hasItem(expected));
    }

    public void testMultipleJodaPatternDeprecationInOneField() throws IOException {
        String simpleMapping = "{\n" +
            "\"properties\" : {\n" +
            "   \"date_time_field\" : {\n" +
            "       \"type\" : \"date\",\n" +
            "       \"format\" : \"Y-C-x-y\"\n" +
            "       }\n" +
            "   }" +
            "}";

        IndexMetaData simpleIndex = createV6Index(simpleMapping);

        DeprecationIssue expected = new DeprecationIssue(DeprecationIssue.Level.WARNING,
            "Date field format uses patterns which has changed meaning in 7.0",
            "https://www.elastic.co/guide/en/elasticsearch/reference/7.0/breaking-changes-7.0.html#breaking_70_java_time_changes",
            "This index has date fields with deprecated formats: ["+
                "[type: _doc, field: date_time_field, format: Y-C-x-y, " +
                "suggestion: 'Y' year-of-era should be replaced with 'y'. Use 'Y' for week-based-year.; " +
                "'y' year should be replaced with 'u'. Use 'y' for year-of-era.; " +
                "'C' century of era is no longer supported.; " +
                "'x' weak-year should be replaced with 'Y'. Use 'x' for zone-offset." +
                "]"+
                "]. "+ JodaDeprecationPatterns.USE_NEW_FORMAT_SPECIFIERS);
        List<DeprecationIssue> issues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(simpleIndex));
        assertThat(issues, hasItem(expected));
    }

    public IndexMetaData createV6Index(String simpleMapping) throws IOException {
        return IndexMetaData.builder(randomAlphaOfLengthBetween(5, 10))
                            .settings(settings(
                                VersionUtils.randomVersionBetween(random(), Version.V_6_0_0,
                                    VersionUtils.getPreviousVersion(Version.V_7_0_0))))
                            .numberOfShards(randomIntBetween(1, 100))
                            .numberOfReplicas(randomIntBetween(1, 100))
                            .putMapping("_doc", simpleMapping)
                            .build();
    }

    static void addRandomFields(final int fieldLimit,
                                XContentBuilder mappingBuilder) throws IOException {
        AtomicInteger fieldCount = new AtomicInteger(0);
        List<String> existingFieldNames = new ArrayList<>();
        while (fieldCount.get() < fieldLimit) {
            addRandomField(existingFieldNames, fieldLimit, mappingBuilder, fieldCount);
        }
    }

    private static void addRandomField(List<String> existingFieldNames, final int fieldLimit,
                                       XContentBuilder mappingBuilder, AtomicInteger fieldCount) throws IOException {
        if (fieldCount.get() > fieldLimit) {
            return;
        }
        String newField = randomValueOtherThanMany(existingFieldNames::contains, () -> randomAlphaOfLengthBetween(2, 20));
        existingFieldNames.add(newField);
        mappingBuilder.startObject(newField);
        {
            if (rarely()) {
                mappingBuilder.startObject("properties");
                {
                    int subfields = randomIntBetween(1, 10);
                    while (existingFieldNames.size() < subfields && fieldCount.get() <= fieldLimit) {
                        addRandomField(existingFieldNames, fieldLimit, mappingBuilder, fieldCount);
                    }
                }
                mappingBuilder.endObject();
            } else {
                mappingBuilder.field("type", randomFrom("array", "range", "boolean", "date", "ip", "keyword", "text"));
                fieldCount.incrementAndGet();
            }
        }
        mappingBuilder.endObject();
    }

    public void testTranslogRetentionSettings() {
        Settings.Builder settings = settings(Version.CURRENT);
        settings.put(IndexSettings.INDEX_SOFT_DELETES_SETTING.getKey(), true);
        settings.put(IndexSettings.INDEX_TRANSLOG_RETENTION_AGE_SETTING.getKey(), randomPositiveTimeValue());
        settings.put(IndexSettings.INDEX_TRANSLOG_RETENTION_SIZE_SETTING.getKey(), between(1, 1024) + "b");
        IndexMetaData indexMetaData = IndexMetaData.builder("test").settings(settings).numberOfShards(1).numberOfReplicas(0).build();
        List<DeprecationIssue> issues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(indexMetaData));
        assertThat(issues, contains(
            new DeprecationIssue(DeprecationIssue.Level.WARNING,
                "translog retention settings are ignored",
                "https://www.elastic.co/guide/en/elasticsearch/reference/current/index-modules-translog.html",
                "translog retention settings [index.translog.retention.size] and [index.translog.retention.age] are ignored " +
                    "because translog is no longer used in peer recoveries with soft-deletes enabled (default in 7.0 or later)")
        ));
    }

    public void testDefaultTranslogRetentionSettings() {
        Settings.Builder settings = settings(Version.CURRENT);
        if (randomBoolean()) {
            settings.put(IndexSettings.INDEX_TRANSLOG_RETENTION_AGE_SETTING.getKey(), randomPositiveTimeValue());
            settings.put(IndexSettings.INDEX_TRANSLOG_RETENTION_SIZE_SETTING.getKey(), between(1, 1024) + "b");
            settings.put(IndexSettings.INDEX_SOFT_DELETES_SETTING.getKey(), false);
        }
        IndexMetaData indexMetaData = IndexMetaData.builder("test").settings(settings).numberOfShards(1).numberOfReplicas(0).build();
        List<DeprecationIssue> issues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(indexMetaData));
        assertThat(issues, empty());
    }

    public void testFieldNamesEnabling() throws IOException {
        XContentBuilder xContent = XContentFactory.jsonBuilder().startObject()
            .startObject(FieldNamesFieldMapper.NAME)
                .field("enabled", randomBoolean())
            .endObject()
        .endObject();
        String mapping = BytesReference.bytes(xContent).utf8ToString();

        IndexMetaData simpleIndex = IndexMetaData.builder(randomAlphaOfLengthBetween(5, 10))
                .settings(settings(
                        VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, Version.CURRENT)))
                .numberOfShards(1)
                .numberOfReplicas(0)
                .putMapping("_doc", mapping).build();
        List<DeprecationIssue> issues = DeprecationChecks.filterChecks(INDEX_SETTINGS_CHECKS, c -> c.apply(simpleIndex));
        assertEquals(1, issues.size());

        DeprecationIssue issue = issues.get(0);
        assertEquals(DeprecationIssue.Level.WARNING, issue.getLevel());
        assertEquals("https://www.elastic.co/guide/en/elasticsearch/reference/master/breaking-changes-8.0.html#fieldnames-enabling"
                , issue.getUrl());
        assertEquals("Index mapping contains explicit `_field_names` enabling settings.", issue.getMessage());
        assertEquals("The index mapping contains a deprecated `enabled` setting for `_field_names` that should be removed moving foward.",
                issue.getDetails());
    }
}
