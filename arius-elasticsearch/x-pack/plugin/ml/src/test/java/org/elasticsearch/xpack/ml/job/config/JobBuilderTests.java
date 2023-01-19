/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.job.config;

import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.AbstractSerializingTestCase;
import org.elasticsearch.xpack.core.ml.job.config.AnalysisConfigTests;
import org.elasticsearch.xpack.core.ml.job.config.AnalysisLimitsTests;
import org.elasticsearch.xpack.core.ml.job.config.DataDescription;
import org.elasticsearch.xpack.core.ml.job.config.Job;
import org.elasticsearch.xpack.core.ml.job.config.ModelPlotConfig;

import java.util.Collections;
import java.util.Date;

import static org.elasticsearch.xpack.core.ml.job.config.JobTests.randomValidJobId;

public class JobBuilderTests extends AbstractSerializingTestCase<Job.Builder> {
    @Override
    protected Job.Builder createTestInstance() {
        Job.Builder builder = new Job.Builder();
        if (randomBoolean()) {
            builder.setId(randomValidJobId());
        }
        if (randomBoolean()) {
            builder.setDescription(randomAlphaOfLength(10));
        }
        if (randomBoolean()) {
            builder.setCreateTime(new Date(randomNonNegativeLong()));
        }
        if (randomBoolean()) {
            builder.setFinishedTime(new Date(randomNonNegativeLong()));
        }
        if (randomBoolean()) {
            builder.setAnalysisConfig(AnalysisConfigTests.createRandomized());
        }
        if (randomBoolean()) {
            builder.setAnalysisLimits(AnalysisLimitsTests.createRandomized());
        }
        if (randomBoolean()) {
            DataDescription.Builder dataDescription = new DataDescription.Builder();
            dataDescription.setFormat(randomFrom(DataDescription.DataFormat.values()));
            builder.setDataDescription(dataDescription);
        }
        if (randomBoolean()) {
            builder.setModelPlotConfig(new ModelPlotConfig(randomBoolean(),
                    randomAlphaOfLength(10)));
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
            builder.setCustomSettings(Collections.singletonMap(randomAlphaOfLength(10),
                    randomAlphaOfLength(10)));
        }
        if (randomBoolean()) {
            builder.setModelSnapshotId(randomAlphaOfLength(10));
        }
        if (randomBoolean()) {
            builder.setResultsIndexName(randomValidJobId());
        }
        return builder;
    }

    @Override
    protected Writeable.Reader<Job.Builder> instanceReader() {
        return Job.Builder::new;
    }

    @Override
    protected Job.Builder doParseInstance(XContentParser parser) {
        return Job.STRICT_PARSER.apply(parser, null);
    }
}
