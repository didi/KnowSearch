/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.job.config;

import com.carrotsearch.randomizedtesting.generators.CodepointSetGenerator;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.Version;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParseException;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.AbstractSerializingTestCase;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.core.ml.MachineLearningField;
import org.elasticsearch.xpack.core.ml.job.messages.Messages;
import org.elasticsearch.xpack.core.ml.job.persistence.AnomalyDetectorsIndexFields;
import org.elasticsearch.xpack.core.ml.job.process.autodetect.state.DataCounts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class JobTests extends AbstractSerializingTestCase<Job> {

    private static final String FUTURE_JOB = "{\n" +
            "    \"job_id\": \"farequote\",\n" +
            "    \"create_time\": 1234567890000,\n" +
            "    \"tomorrows_technology_today\": \"wow\",\n" +
            "    \"analysis_config\": {\n" +
            "        \"bucket_span\": \"1h\",\n" +
            "        \"something_new\": \"gasp\",\n" +
            "        \"detectors\": [{\"function\": \"metric\", \"field_name\": \"responsetime\", \"by_field_name\": \"airline\"}]\n" +
            "    },\n" +
            "    \"data_description\": {\n" +
            "        \"time_field\": \"time\",\n" +
            "        \"the_future\": 123\n" +
            "    }\n" +
            "}";

    @Override
    protected Job createTestInstance() {
        return createRandomizedJob();
    }

    @Override
    protected Writeable.Reader<Job> instanceReader() {
        return Job::new;
    }

    @Override
    protected Job doParseInstance(XContentParser parser) {
        return Job.STRICT_PARSER.apply(parser, null).build();
    }

    public void testFutureConfigParse() throws IOException {
        XContentParser parser = XContentFactory.xContent(XContentType.JSON)
                .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, FUTURE_JOB);
        XContentParseException e = expectThrows(XContentParseException.class,
                () -> Job.STRICT_PARSER.apply(parser, null).build());
        assertEquals("[4:5] [job_details] unknown field [tomorrows_technology_today]", e.getMessage());
    }

    public void testFutureMetadataParse() throws IOException {
        XContentParser parser = XContentFactory.xContent(XContentType.JSON)
                .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, FUTURE_JOB);
        // Unlike the config version of this test, the metadata parser should tolerate the unknown future field
        assertNotNull(Job.LENIENT_PARSER.apply(parser, null).build());
    }

    public void testConstructor_GivenEmptyJobConfiguration() {
        Job job = buildJobBuilder("foo").build();

        assertEquals("foo", job.getId());
        assertNotNull(job.getCreateTime());
        assertNotNull(job.getAnalysisConfig());
        assertNotNull(job.getAnalysisLimits());
        assertNull(job.getCustomSettings());
        assertNotNull(job.getDataDescription());
        assertNull(job.getDescription());
        assertNull(job.getFinishedTime());
        assertNull(job.getModelPlotConfig());
        assertNull(job.getRenormalizationWindowDays());
        assertNull(job.getBackgroundPersistInterval());
        assertThat(job.getModelSnapshotRetentionDays(), equalTo(1L));
        assertNull(job.getResultsRetentionDays());
        assertNotNull(job.allInputFields());
        assertFalse(job.allInputFields().isEmpty());
        assertFalse(job.allowLazyOpen());
    }

    public void testNoId() {
        expectThrows(IllegalArgumentException.class, () -> buildJobBuilder("").build());
    }

    public void testEnsureModelMemoryLimitSet() {
        Job.Builder builder = buildJobBuilder("foo");
        builder.setAnalysisLimits(new AnalysisLimits(null, null));
        builder.validateAnalysisLimitsAndSetDefaults(new ByteSizeValue(0L));
        Job job = builder.build();
        assertEquals("foo", job.getId());
        assertNotNull(job.getAnalysisLimits());
        assertThat(job.getAnalysisLimits().getModelMemoryLimit(), equalTo(AnalysisLimits.DEFAULT_MODEL_MEMORY_LIMIT_MB));
        assertThat(job.getAnalysisLimits().getCategorizationExamplesLimit(), equalTo(4L));

        builder.setAnalysisLimits(new AnalysisLimits(AnalysisLimits.DEFAULT_MODEL_MEMORY_LIMIT_MB * 2, 5L));
        builder.validateAnalysisLimitsAndSetDefaults(null);
        job = builder.build();
        assertNotNull(job.getAnalysisLimits());
        assertThat(job.getAnalysisLimits().getModelMemoryLimit(), equalTo(AnalysisLimits.DEFAULT_MODEL_MEMORY_LIMIT_MB * 2));
        assertThat(job.getAnalysisLimits().getCategorizationExamplesLimit(), equalTo(5L));
    }

    public void testValidateAnalysisLimitsAndSetDefaults_whenMaxIsLessThanTheDefault() {
        Job.Builder builder = buildJobBuilder("foo");
        builder.validateAnalysisLimitsAndSetDefaults(new ByteSizeValue(512L, ByteSizeUnit.MB));

        Job job = builder.build();
        assertNotNull(job.getAnalysisLimits());
        assertThat(job.getAnalysisLimits().getModelMemoryLimit(), equalTo(512L));
        assertThat(job.getAnalysisLimits().getCategorizationExamplesLimit(), equalTo(4L));
    }

    public void testValidateAnalysisLimitsAndSetDefaults_throwsWhenMaxLimitIsExceeded() {
        Job.Builder builder = buildJobBuilder("foo");
        builder.setAnalysisLimits(new AnalysisLimits(4096L, null));
        ElasticsearchStatusException e = expectThrows(ElasticsearchStatusException.class,
                () -> builder.validateAnalysisLimitsAndSetDefaults(new ByteSizeValue(1000L, ByteSizeUnit.MB)));
        assertEquals("model_memory_limit [4gb] must be less than the value of the " +
                MachineLearningField.MAX_MODEL_MEMORY_LIMIT.getKey() + " setting [1000mb]", e.getMessage());

        builder.validateAnalysisLimitsAndSetDefaults(new ByteSizeValue(8192L, ByteSizeUnit.MB));
    }

    public void testEquals_GivenDifferentClass() {
        Job job = buildJobBuilder("foo").build();
        assertFalse(job.equals("a string"));
    }

    public void testEquals_GivenDifferentIds() {
        Date createTime = new Date();
        Job.Builder builder = buildJobBuilder("foo");
        builder.setCreateTime(createTime);
        Job job1 = builder.build();
        builder.setId("bar");
        Job job2 = builder.build();
        assertFalse(job1.equals(job2));
    }

    public void testEquals_GivenDifferentRenormalizationWindowDays() {
        Date date = new Date();
        Job.Builder jobDetails1 = new Job.Builder("foo");
        jobDetails1.setDataDescription(new DataDescription.Builder());
        jobDetails1.setAnalysisConfig(createAnalysisConfig());
        jobDetails1.setRenormalizationWindowDays(3L);
        jobDetails1.setCreateTime(date);
        Job.Builder jobDetails2 = new Job.Builder("foo");
        jobDetails2.setDataDescription(new DataDescription.Builder());
        jobDetails2.setRenormalizationWindowDays(4L);
        jobDetails2.setAnalysisConfig(createAnalysisConfig());
        jobDetails2.setCreateTime(date);
        assertFalse(jobDetails1.build().equals(jobDetails2.build()));
    }

    public void testEquals_GivenDifferentBackgroundPersistInterval() {
        Date date = new Date();
        Job.Builder jobDetails1 = new Job.Builder("foo");
        jobDetails1.setDataDescription(new DataDescription.Builder());
        jobDetails1.setAnalysisConfig(createAnalysisConfig());
        jobDetails1.setBackgroundPersistInterval(TimeValue.timeValueSeconds(10000L));
        jobDetails1.setCreateTime(date);
        Job.Builder jobDetails2 = new Job.Builder("foo");
        jobDetails2.setDataDescription(new DataDescription.Builder());
        jobDetails2.setBackgroundPersistInterval(TimeValue.timeValueSeconds(8000L));
        jobDetails2.setAnalysisConfig(createAnalysisConfig());
        jobDetails2.setCreateTime(date);
        assertFalse(jobDetails1.build().equals(jobDetails2.build()));
    }

    public void testEquals_GivenDifferentModelSnapshotRetentionDays() {
        Date date = new Date();
        Job.Builder jobDetails1 = new Job.Builder("foo");
        jobDetails1.setDataDescription(new DataDescription.Builder());
        jobDetails1.setAnalysisConfig(createAnalysisConfig());
        jobDetails1.setModelSnapshotRetentionDays(10L);
        jobDetails1.setCreateTime(date);
        Job.Builder jobDetails2 = new Job.Builder("foo");
        jobDetails2.setDataDescription(new DataDescription.Builder());
        jobDetails2.setModelSnapshotRetentionDays(8L);
        jobDetails2.setAnalysisConfig(createAnalysisConfig());
        jobDetails2.setCreateTime(date);
        assertFalse(jobDetails1.build().equals(jobDetails2.build()));
    }

    public void testEquals_GivenDifferentResultsRetentionDays() {
        Date date = new Date();
        Job.Builder jobDetails1 = new Job.Builder("foo");
        jobDetails1.setDataDescription(new DataDescription.Builder());
        jobDetails1.setAnalysisConfig(createAnalysisConfig());
        jobDetails1.setCreateTime(date);
        jobDetails1.setResultsRetentionDays(30L);
        Job.Builder jobDetails2 = new Job.Builder("foo");
        jobDetails2.setDataDescription(new DataDescription.Builder());
        jobDetails2.setResultsRetentionDays(4L);
        jobDetails2.setAnalysisConfig(createAnalysisConfig());
        jobDetails2.setCreateTime(date);
        assertFalse(jobDetails1.build().equals(jobDetails2.build()));
    }

    public void testEquals_GivenDifferentCustomSettings() {
        Job.Builder jobDetails1 = buildJobBuilder("foo");
        Map<String, Object> customSettings1 = new HashMap<>();
        customSettings1.put("key1", "value1");
        jobDetails1.setCustomSettings(customSettings1);
        Job.Builder jobDetails2 = buildJobBuilder("foo");
        Map<String, Object> customSettings2 = new HashMap<>();
        customSettings2.put("key2", "value2");
        jobDetails2.setCustomSettings(customSettings2);
        assertFalse(jobDetails1.build().equals(jobDetails2.build()));
    }

    // JobConfigurationTests:

    /**
     * Test the {@link AnalysisConfig#analysisFields()} method which produces a
     * list of analysis fields from the detectors
     */
    public void testAnalysisConfigRequiredFields() {
        Detector.Builder d1 = new Detector.Builder("max", "field");
        d1.setByFieldName("by_field");

        Detector.Builder d2 = new Detector.Builder("median", "field2");
        d2.setOverFieldName("over_field");

        AnalysisConfig.Builder ac = new AnalysisConfig.Builder(Arrays.asList(d1.build(), d2.build()));
        ac.setSummaryCountFieldName("agg");

        Set<String> analysisFields = ac.build().analysisFields();
        assertTrue(analysisFields.size() == 5);

        assertTrue(analysisFields.contains("agg"));
        assertTrue(analysisFields.contains("field"));
        assertTrue(analysisFields.contains("by_field"));
        assertTrue(analysisFields.contains("field2"));
        assertTrue(analysisFields.contains("over_field"));

        assertFalse(analysisFields.contains("max"));
        assertFalse(analysisFields.contains("median"));
        assertFalse(analysisFields.contains(""));

        Detector.Builder d3 = new Detector.Builder("count", null);
        d3.setByFieldName("by2");
        d3.setPartitionFieldName("partition");

        ac = new AnalysisConfig.Builder(Arrays.asList(d1.build(), d2.build(), d3.build()));

        analysisFields = ac.build().analysisFields();
        assertTrue(analysisFields.size() == 6);

        assertTrue(analysisFields.contains("partition"));
        assertTrue(analysisFields.contains("field"));
        assertTrue(analysisFields.contains("by_field"));
        assertTrue(analysisFields.contains("by2"));
        assertTrue(analysisFields.contains("field2"));
        assertTrue(analysisFields.contains("over_field"));

        assertFalse(analysisFields.contains("count"));
        assertFalse(analysisFields.contains("max"));
        assertFalse(analysisFields.contains("median"));
        assertFalse(analysisFields.contains(""));
    }

    // JobConfigurationVerifierTests:

    public void testCopyConstructor() {
        for (int i = 0; i < NUMBER_OF_TEST_RUNS; i++) {
            Job job = createTestInstance();
            Job copy = new Job.Builder(job).build();
            assertEquals(job, copy);
        }
    }

    public void testCheckValidId_IdTooLong()  {
        Job.Builder builder = buildJobBuilder("foo");
        builder.setId("averyveryveryaveryveryveryaveryveryveryaveryveryveryaveryveryveryaveryveryverylongid");
        expectThrows(IllegalArgumentException.class, builder::build);
    }

    public void testCheckValidId_GivenAllValidChars() {
        Job.Builder builder = buildJobBuilder("foo");
        builder.setId("abcdefghijklmnopqrstuvwxyz-._0123456789");
        builder.build();
    }

    public void testCheckValidId_ProhibitedChars() {
        String invalidChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()+?\"'~±/\\[]{},<>=";
        Job.Builder builder = buildJobBuilder("foo");
        for (char c : invalidChars.toCharArray()) {
            builder.setId(Character.toString(c));
            String errorMessage = Messages.getMessage(Messages.INVALID_ID, Job.ID.getPreferredName(), Character.toString(c));
            IllegalArgumentException e = expectThrows(IllegalArgumentException.class, builder::build);
            assertEquals(errorMessage, e.getMessage());
        }
    }

    public void testCheckValidId_startsWithUnderscore() {
        Job.Builder builder = buildJobBuilder("_foo");
        String errorMessage = Messages.getMessage(Messages.INVALID_ID, Job.ID.getPreferredName(), "_foo");
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, builder::build);
        assertEquals(errorMessage, e.getMessage());
    }

    public void testCheckValidId_endsWithUnderscore() {
        Job.Builder builder = buildJobBuilder("foo_");
        String errorMessage = Messages.getMessage(Messages.INVALID_ID, Job.ID.getPreferredName(), "foo_");
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, builder::build);
        assertEquals(errorMessage, e.getMessage());
    }

    public void testCheckValidId_ControlChars() {
        Job.Builder builder = buildJobBuilder("foo");
        builder.setId("two\nlines");
        expectThrows(IllegalArgumentException.class, builder::build);
    }

    public void jobConfigurationTest() {
        Job.Builder builder = new Job.Builder();
        expectThrows(IllegalArgumentException.class, builder::build);
        builder.setId("bad id with spaces");
        expectThrows(IllegalArgumentException.class, builder::build);
        builder.setId("bad_id_with_UPPERCASE_chars");
        expectThrows(IllegalArgumentException.class, builder::build);
        builder.setId("a very  very very very very very very very very very very very very very very very very very very very long id");
        expectThrows(IllegalArgumentException.class, builder::build);
        builder.setId(null);
        expectThrows(IllegalArgumentException.class, builder::build);

        Detector.Builder d = new Detector.Builder("max", "a");
        d.setByFieldName("b");
        AnalysisConfig.Builder ac = new AnalysisConfig.Builder(Collections.singletonList(d.build()));
        builder.setAnalysisConfig(ac);
        builder.build();
        builder.setAnalysisLimits(new AnalysisLimits(-1L, null));
        expectThrows(IllegalArgumentException.class, builder::build);
        AnalysisLimits limits = new AnalysisLimits(1000L, 4L);
        builder.setAnalysisLimits(limits);
        builder.build();
        DataDescription.Builder dc = new DataDescription.Builder();
        dc.setTimeFormat("YYY_KKKKajsatp*");
        builder.setDataDescription(dc);
        expectThrows(IllegalArgumentException.class, builder::build);
        dc = new DataDescription.Builder();
        builder.setDataDescription(dc);
        expectThrows(IllegalArgumentException.class, builder::build);
        builder.build();
    }

    public void testVerify_GivenNegativeRenormalizationWindowDays() {
        String errorMessage = Messages.getMessage(Messages.JOB_CONFIG_FIELD_VALUE_TOO_LOW,
                "renormalization_window_days", 0, -1);
        Job.Builder builder = buildJobBuilder("foo");
        builder.setRenormalizationWindowDays(-1L);
        IllegalArgumentException e = ESTestCase.expectThrows(IllegalArgumentException.class, builder::build);
        assertEquals(errorMessage, e.getMessage());
    }

    public void testVerify_GivenNegativeModelSnapshotRetentionDays() {
        String errorMessage = Messages.getMessage(Messages.JOB_CONFIG_FIELD_VALUE_TOO_LOW, "model_snapshot_retention_days", 0, -1);
        Job.Builder builder = buildJobBuilder("foo");
        builder.setModelSnapshotRetentionDays(-1L);
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, builder::build);

        assertEquals(errorMessage, e.getMessage());
    }

    public void testVerify_GivenLowBackgroundPersistInterval() {
        String errorMessage = Messages.getMessage(Messages.JOB_CONFIG_FIELD_VALUE_TOO_LOW, "background_persist_interval", 3600, 3599);
        Job.Builder builder = buildJobBuilder("foo");
        builder.setBackgroundPersistInterval(TimeValue.timeValueSeconds(3599L));
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, builder::build);
        assertEquals(errorMessage, e.getMessage());
    }

    public void testVerify_GivenNegativeResultsRetentionDays() {
        String errorMessage = Messages.getMessage(Messages.JOB_CONFIG_FIELD_VALUE_TOO_LOW,
                "results_retention_days", 0, -1);
        Job.Builder builder = buildJobBuilder("foo");
        builder.setResultsRetentionDays(-1L);
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, builder::build);
        assertEquals(errorMessage, e.getMessage());
    }

    public void testBuilder_setsDefaultIndexName() {
        Job.Builder builder = buildJobBuilder("foo");
        Job job = builder.build();
        assertEquals(AnomalyDetectorsIndexFields.RESULTS_INDEX_PREFIX + AnomalyDetectorsIndexFields.RESULTS_INDEX_DEFAULT,
                job.getInitialResultsIndexName());
    }

    public void testBuilder_setsIndexName() {
        Job.Builder builder = buildJobBuilder("foo");
        builder.setResultsIndexName("carol");
        Job job = builder.build();
        assertEquals(AnomalyDetectorsIndexFields.RESULTS_INDEX_PREFIX + "custom-carol", job.getInitialResultsIndexName());
    }

    public void testBuilder_withInvalidIndexNameThrows() {
        Job.Builder builder = buildJobBuilder("foo");
        builder.setResultsIndexName("_bad^name");
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, builder::build);
        assertEquals(Messages.getMessage(Messages.INVALID_ID, Job.RESULTS_INDEX_NAME.getPreferredName(), "_bad^name"), e.getMessage());
    }

    public void testBuilder_buildWithCreateTime() {
        Job.Builder builder = buildJobBuilder("foo");
        Date now = new Date();
        Job job = builder.build(now);
        assertEquals(now, job.getCreateTime());
        assertEquals(Version.CURRENT, job.getJobVersion());
    }

    public void testJobWithoutVersion() throws IOException {
        Job.Builder builder = buildJobBuilder("foo");
        Job job = builder.build();
        assertThat(job.getJobVersion(), is(nullValue()));

        // Assert parsing a job without version works as expected
        XContentType xContentType = randomFrom(XContentType.values());
        BytesReference bytes = XContentHelper.toXContent(job, xContentType, false);
        try(XContentParser parser = createParser(xContentType.xContent(), bytes)) {
            Job parsed = doParseInstance(parser);
            assertThat(parsed, equalTo(job));
        }
    }

    public void testBuilder_buildRequiresDataDescription() {
        Job.Builder builder = new Job.Builder("no-data-description");
        builder.setAnalysisConfig(createAnalysisConfig());

        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, builder::build);
        assertThat(e.getMessage(), equalTo("A data_description must be set"));
    }

    public void testBuilder_givenTimeFieldInAnalysisConfig() {
        DataDescription.Builder dataDescriptionBuilder = new DataDescription.Builder();
        // field name used here matches what's in createAnalysisConfig()
        dataDescriptionBuilder.setTimeField("client");

        Job.Builder jobBuilder = new Job.Builder("time-field-in-analysis-config");
        jobBuilder.setAnalysisConfig(createAnalysisConfig());
        jobBuilder.setDataDescription(dataDescriptionBuilder);

        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, jobBuilder::build);
        assertThat(e.getMessage(), equalTo(Messages.getMessage(Messages.JOB_CONFIG_TIME_FIELD_NOT_ALLOWED_IN_ANALYSIS_CONFIG)));
    }

    public void testInvalidCreateTimeSettings() {
        Job.Builder builder = new Job.Builder("invalid-settings");
        builder.setModelSnapshotId("snapshot-foo");
        assertEquals(Collections.singletonList(Job.MODEL_SNAPSHOT_ID.getPreferredName()), builder.invalidCreateTimeSettings());

        builder.setCreateTime(new Date());
        builder.setFinishedTime(new Date());

        Set<String> expected = new HashSet<>();
        expected.add(Job.CREATE_TIME.getPreferredName());
        expected.add(Job.FINISHED_TIME.getPreferredName());
        expected.add(Job.MODEL_SNAPSHOT_ID.getPreferredName());

        assertEquals(expected, new HashSet<>(builder.invalidCreateTimeSettings()));
    }

    public void testEmptyGroup() {
        Job.Builder builder = buildJobBuilder("foo");
        builder.setGroups(Arrays.asList("foo-group", ""));
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, builder::build);
        assertThat(e.getMessage(), containsString("Invalid group id ''"));
    }

    public void testInvalidGroup() {
        Job.Builder builder = buildJobBuilder("foo");
        builder.setGroups(Arrays.asList("foo-group", "$$$"));
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, builder::build);
        assertThat(e.getMessage(), containsString("Invalid group id '$$$'"));
    }

    public void testInvalidGroup_matchesJobId() {
        Job.Builder builder = buildJobBuilder("foo");
        builder.setGroups(Collections.singletonList("foo"));
        ResourceAlreadyExistsException e = expectThrows(ResourceAlreadyExistsException.class, builder::build);
        assertEquals(e.getMessage(), "job and group names must be unique but job [foo] and group [foo] have the same name");
    }

    public void testInvalidAnalysisConfig_duplicateDetectors() throws Exception {
        Job.Builder builder =
            new Job.Builder("job_with_duplicate_detectors")
                .setCreateTime(new Date())
                .setDataDescription(new DataDescription.Builder())
                .setAnalysisConfig(new AnalysisConfig.Builder(Arrays.asList(
                    new Detector.Builder("mean", "responsetime").build(),
                    new Detector.Builder("mean", "responsetime").build()
                )));

        IllegalArgumentException e = expectThrows(IllegalArgumentException.class, builder::validateDetectorsAreUnique);
        assertThat(e.getMessage(), containsString("Duplicate detectors are not allowed: [mean(responsetime)]"));
    }

    public void testEarliestValidTimestamp_GivenEmptyDataCounts() {
        assertThat(createRandomizedJob().earliestValidTimestamp(new DataCounts("foo")), equalTo(0L));
    }

    public void testEarliestValidTimestamp_GivenDataCountsAndZeroLatency() {
        Job.Builder builder = buildJobBuilder("foo");
        DataCounts dataCounts = new DataCounts(builder.getId());
        dataCounts.setLatestRecordTimeStamp(new Date(123456789L));

        assertThat(builder.build().earliestValidTimestamp(dataCounts), equalTo(123456789L));
    }

    public void testEarliestValidTimestamp_GivenDataCountsAndLatency() {
        Job.Builder builder = buildJobBuilder("foo");
        AnalysisConfig.Builder analysisConfig = new AnalysisConfig.Builder(builder.build().getAnalysisConfig());
        analysisConfig.setLatency(TimeValue.timeValueMillis(1000L));
        builder.setAnalysisConfig(analysisConfig);

        DataCounts dataCounts = new DataCounts(builder.getId());
        dataCounts.setLatestRecordTimeStamp(new Date(123456789L));

        assertThat(builder.build().earliestValidTimestamp(dataCounts), equalTo(123455789L));
    }

    public void testCopyingJobDoesNotCauseStackOverflow() {
        Job job = createRandomizedJob();
        for (int i = 0; i < 100000; i++) {
            job = new Job.Builder(job).build();
        }
    }

    public static Job.Builder buildJobBuilder(String id, Date date) {
        Job.Builder builder = new Job.Builder(id);
        builder.setCreateTime(date);
        AnalysisConfig.Builder ac = createAnalysisConfig();
        DataDescription.Builder dc = new DataDescription.Builder();
        builder.setAnalysisConfig(ac);
        builder.setDataDescription(dc);
        return builder;
    }

    public static Job.Builder buildJobBuilder(String id) {
        return buildJobBuilder(id, new Date());
    }

    public static String randomValidJobId() {
        CodepointSetGenerator generator =  new CodepointSetGenerator("abcdefghijklmnopqrstuvwxyz".toCharArray());
        return generator.ofCodePointsLength(random(), 10, 10);
    }

    public static AnalysisConfig.Builder createAnalysisConfig() {
        Detector.Builder d1 = new Detector.Builder("info_content", "domain");
        d1.setOverFieldName("client");
        Detector.Builder d2 = new Detector.Builder("min", "field");
        return new AnalysisConfig.Builder(Arrays.asList(d1.build(), d2.build()));
    }

    public static Job createRandomizedJob() {
        String jobId = randomValidJobId();
        Job.Builder builder = new Job.Builder(jobId);
        if (randomBoolean()) {
            builder.setDescription(randomAlphaOfLength(10));
        }
        if (randomBoolean()) {
            builder.setJobVersion(Version.CURRENT);
        }
        if (randomBoolean()) {
            int groupsNum = randomIntBetween(0, 10);
            List<String> groups = new ArrayList<>(groupsNum);
            for (int i = 0; i < groupsNum; i++) {
                groups.add(randomValidJobId());
            }
            builder.setGroups(groups);
        }
        builder.setCreateTime(new Date(randomNonNegativeLong()));
        if (randomBoolean()) {
            builder.setFinishedTime(new Date(randomNonNegativeLong()));
        }
        builder.setAnalysisConfig(AnalysisConfigTests.createRandomized());
        builder.setAnalysisLimits(AnalysisLimits.validateAndSetDefaults(AnalysisLimitsTests.createRandomized(), null,
                AnalysisLimits.DEFAULT_MODEL_MEMORY_LIMIT_MB));

        DataDescription.Builder dataDescription = new DataDescription.Builder();
        dataDescription.setFormat(randomFrom(DataDescription.DataFormat.values()));
        builder.setDataDescription(dataDescription);

        if (randomBoolean()) {
            builder.setModelPlotConfig(new ModelPlotConfig(randomBoolean(), randomAlphaOfLength(10)));
        }
        if (randomBoolean()) {
            builder.setRenormalizationWindowDays(randomNonNegativeLong());
        }
        if (randomBoolean()) {
            builder.setBackgroundPersistInterval(TimeValue.timeValueHours(randomIntBetween(1, 24)));
        }
        if (randomBoolean()) {
            builder.setModelSnapshotRetentionDays(randomNonNegativeLong());
        }
        if (randomBoolean()) {
            builder.setResultsRetentionDays(randomNonNegativeLong());
        }
        if (randomBoolean()) {
            builder.setCustomSettings(Collections.singletonMap(randomAlphaOfLength(10), randomAlphaOfLength(10)));
        }
        if (randomBoolean()) {
            builder.setModelSnapshotId(randomAlphaOfLength(10));
        }
        if (randomBoolean()) {
            builder.setModelSnapshotMinVersion(Version.CURRENT);
        }
        if (randomBoolean()) {
            builder.setResultsIndexName(randomValidJobId());
        }
        if (randomBoolean()) {
            builder.setAllowLazyOpen(randomBoolean());
        }
        return builder.build();
    }
}
