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

package org.elasticsearch.client;

import org.elasticsearch.action.ingest.DeletePipelineRequest;
import org.elasticsearch.action.ingest.GetPipelineRequest;
import org.elasticsearch.action.ingest.GetPipelineResponse;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.ingest.SimulateDocumentBaseResult;
import org.elasticsearch.action.ingest.SimulateDocumentResult;
import org.elasticsearch.action.ingest.SimulateDocumentVerboseResult;
import org.elasticsearch.action.ingest.SimulatePipelineRequest;
import org.elasticsearch.action.ingest.SimulatePipelineResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.ingest.PipelineConfiguration;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class IngestClientIT extends ESRestHighLevelClientTestCase {

    public void testPutPipeline() throws IOException {
        String id = "some_pipeline_id";
        XContentBuilder pipelineBuilder = buildRandomXContentPipeline();
        PutPipelineRequest request = new PutPipelineRequest(
            id,
            BytesReference.bytes(pipelineBuilder),
            pipelineBuilder.contentType());

        AcknowledgedResponse putPipelineResponse =
            execute(request, highLevelClient().ingest()::putPipeline, highLevelClient().ingest()::putPipelineAsync);
        assertTrue(putPipelineResponse.isAcknowledged());
    }

    public void testGetPipeline() throws IOException {
        String id = "some_pipeline_id";
        XContentBuilder pipelineBuilder = buildRandomXContentPipeline();
        {
            PutPipelineRequest request = new PutPipelineRequest(
                id,
                BytesReference.bytes(pipelineBuilder),
                pipelineBuilder.contentType()
            );
            createPipeline(request);
        }

        GetPipelineRequest request = new GetPipelineRequest(id);

        GetPipelineResponse response =
            execute(request, highLevelClient().ingest()::getPipeline, highLevelClient().ingest()::getPipelineAsync);
        assertTrue(response.isFound());
        assertEquals(response.pipelines().get(0).getId(), id);
        PipelineConfiguration expectedConfig =
            new PipelineConfiguration(id, BytesReference.bytes(pipelineBuilder), pipelineBuilder.contentType());
        assertEquals(expectedConfig.getConfigAsMap(), response.pipelines().get(0).getConfigAsMap());
    }

    public void testGetNonexistentPipeline() throws IOException {
        String id = "nonexistent_pipeline_id";

        GetPipelineRequest request = new GetPipelineRequest(id);

        GetPipelineResponse response =
            execute(request, highLevelClient().ingest()::getPipeline, highLevelClient().ingest()::getPipelineAsync);
        assertFalse(response.isFound());
    }

    public void testDeletePipeline() throws IOException {
        String id = "some_pipeline_id";
        {
            createPipeline(id);
        }

        DeletePipelineRequest request = new DeletePipelineRequest(id);

        AcknowledgedResponse response =
            execute(request, highLevelClient().ingest()::deletePipeline, highLevelClient().ingest()::deletePipelineAsync);
        assertTrue(response.isAcknowledged());
    }

    public void testSimulatePipeline() throws IOException {
        testSimulatePipeline(false, false);
    }

    public void testSimulatePipelineWithFailure() throws IOException {
        testSimulatePipeline(false, true);
    }

    public void testSimulatePipelineVerbose() throws IOException {
        testSimulatePipeline(true, false);
    }

    public void testSimulatePipelineVerboseWithFailure() throws IOException {
        testSimulatePipeline(true, true);
    }

    private void testSimulatePipeline(boolean isVerbose,
                                      boolean isFailure) throws IOException {
        XContentType xContentType = randomFrom(XContentType.values());
        XContentBuilder builder = XContentBuilder.builder(xContentType.xContent());
        String rankValue = isFailure ? "non-int" : Integer.toString(1234);
        builder.startObject();
        {
            builder.field("pipeline");
            buildRandomXContentPipeline(builder);
            builder.startArray("docs");
            {
                builder.startObject()
                    .field("_index", "index")
                    .field("_id", "doc_" + 1)
                    .startObject("_source").field("foo", "rab_" + 1).field("rank", rankValue).endObject()
                    .endObject();
            }
            builder.endArray();
        }
        builder.endObject();

        SimulatePipelineRequest request = new SimulatePipelineRequest(
            BytesReference.bytes(builder),
            builder.contentType()
        );
        request.setVerbose(isVerbose);
        SimulatePipelineResponse response =
            execute(request, highLevelClient().ingest()::simulate, highLevelClient().ingest()::simulateAsync);
        List<SimulateDocumentResult> results = response.getResults();
        assertEquals(1, results.size());
        if (isVerbose) {
            assertThat(results.get(0), instanceOf(SimulateDocumentVerboseResult.class));
            SimulateDocumentVerboseResult verboseResult = (SimulateDocumentVerboseResult) results.get(0);
            assertEquals(2, verboseResult.getProcessorResults().size());
            if (isFailure) {
                assertNotNull(verboseResult.getProcessorResults().get(1).getFailure());
                assertThat(verboseResult.getProcessorResults().get(1).getFailure().getMessage(),
                    containsString("unable to convert [non-int] to integer"));
            } else {
                assertEquals(
                    verboseResult.getProcessorResults().get(0).getIngestDocument()
                        .getFieldValue("foo", String.class),
                    "bar"
                );
                assertEquals(
                    Integer.valueOf(1234),
                    verboseResult.getProcessorResults().get(1).getIngestDocument()
                        .getFieldValue("rank", Integer.class)
                );
            }
        } else {
            assertThat(results.get(0), instanceOf(SimulateDocumentBaseResult.class));
            SimulateDocumentBaseResult baseResult = (SimulateDocumentBaseResult)results.get(0);
            if (isFailure) {
                assertNotNull(baseResult.getFailure());
                assertThat(baseResult.getFailure().getMessage(),
                    containsString("unable to convert [non-int] to integer"));
            } else {
                assertNotNull(baseResult.getIngestDocument());
                assertEquals(
                    baseResult.getIngestDocument().getFieldValue("foo", String.class),
                    "bar"
                );
                assertEquals(
                    Integer.valueOf(1234),
                    baseResult.getIngestDocument()
                        .getFieldValue("rank", Integer.class)
                );
            }
        }
    }
}
