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

package org.elasticsearch.client.rollup;

import org.elasticsearch.client.core.IndexerJobStats;
import org.elasticsearch.client.core.IndexerState;
import org.elasticsearch.client.rollup.GetRollupJobResponse.JobWrapper;
import org.elasticsearch.client.rollup.GetRollupJobResponse.RollupIndexerJobStats;
import org.elasticsearch.client.rollup.GetRollupJobResponse.RollupJobStatus;
import org.elasticsearch.client.rollup.job.config.RollupJobConfigTests;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.test.AbstractXContentTestCase.xContentTester;

public class GetRollupJobResponseTests extends ESTestCase {
    public void testFromXContent() throws IOException {
        xContentTester(
                this::createParser,
                this::createTestInstance,
                this::toXContent,
                GetRollupJobResponse::fromXContent)
                .supportsUnknownFields(false)
                .randomFieldsExcludeFilter(field ->
                        field.endsWith("status.current_position"))
                .test();
    }

    private GetRollupJobResponse createTestInstance() {
        int jobCount = between(1, 5);
        List<JobWrapper> jobs = new ArrayList<>();
        for (int j = 0; j < jobCount; j++) {
            jobs.add(new JobWrapper(
                    RollupJobConfigTests.randomRollupJobConfig(randomAlphaOfLength(5)),
                    randomStats(),
                    randomStatus()));
        }
        return new GetRollupJobResponse(jobs);
    }

    private RollupIndexerJobStats randomStats() {
        return new RollupIndexerJobStats(randomNonNegativeLong(), randomNonNegativeLong(), randomNonNegativeLong(),
            randomNonNegativeLong(), randomNonNegativeLong(), randomNonNegativeLong(),
            randomNonNegativeLong(), randomNonNegativeLong(), randomNonNegativeLong(), randomNonNegativeLong());
    }

    private RollupJobStatus randomStatus() {
        Map<String, Object> currentPosition = new HashMap<>();
        int positions = between(0, 10);
        while (currentPosition.size() < positions) {
            currentPosition.put(randomAlphaOfLength(2), randomAlphaOfLength(2));
        }
        return new RollupJobStatus(
            randomFrom(IndexerState.values()),
            currentPosition,
            randomBoolean());
    }

    private void toXContent(GetRollupJobResponse response, XContentBuilder builder) throws IOException {
        ToXContent.Params params = ToXContent.EMPTY_PARAMS;
        builder.startObject();
        builder.startArray(GetRollupJobResponse.JOBS.getPreferredName());
        for (JobWrapper job : response.getJobs()) {
            toXContent(job, builder, params);
        }
        builder.endArray();
        builder.endObject();
    }

    private void toXContent(JobWrapper jobWrapper, XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject();
        builder.field(GetRollupJobResponse.CONFIG.getPreferredName());
        jobWrapper.getJob().toXContent(builder, params);
        builder.field(GetRollupJobResponse.STATUS.getPreferredName());
        toXContent(jobWrapper.getStatus(), builder, params);
        builder.field(GetRollupJobResponse.STATS.getPreferredName());
        toXContent(jobWrapper.getStats(), builder, params);
        builder.endObject();
    }

    public void toXContent(RollupJobStatus status, XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject();
        builder.field(GetRollupJobResponse.STATE.getPreferredName(), status.getState().value());
        if (status.getCurrentPosition() != null) {
            builder.field(GetRollupJobResponse.CURRENT_POSITION.getPreferredName(), status.getCurrentPosition());
        }
        builder.field(GetRollupJobResponse.UPGRADED_DOC_ID.getPreferredName(), status.getUpgradedDocumentId());
        builder.endObject();
    }

    public void toXContent(RollupIndexerJobStats stats, XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject();
        builder.field(IndexerJobStats.NUM_PAGES.getPreferredName(), stats.getNumPages());
        builder.field(IndexerJobStats.NUM_INPUT_DOCUMENTS.getPreferredName(), stats.getNumDocuments());
        builder.field(GetRollupJobResponse.ROLLUPS_INDEXED.getPreferredName(), stats.getOutputDocuments());
        builder.field(IndexerJobStats.NUM_INVOCATIONS.getPreferredName(), stats.getNumInvocations());
        builder.field(IndexerJobStats.INDEX_TIME_IN_MS.getPreferredName(), stats.getIndexTime());
        builder.field(IndexerJobStats.INDEX_TOTAL.getPreferredName(), stats.getIndexTotal());
        builder.field(IndexerJobStats.INDEX_FAILURES.getPreferredName(), stats.getIndexFailures());
        builder.field(IndexerJobStats.SEARCH_TIME_IN_MS.getPreferredName(), stats.getSearchTime());
        builder.field(IndexerJobStats.SEARCH_TOTAL.getPreferredName(), stats.getSearchTotal());
        builder.field(IndexerJobStats.SEARCH_FAILURES.getPreferredName(), stats.getSearchFailures());
        builder.endObject();
    }

}
