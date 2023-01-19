/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.action;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.ingest.IngestStats;
import org.elasticsearch.xpack.core.action.AbstractGetResourcesRequest;
import org.elasticsearch.xpack.core.action.AbstractGetResourcesResponse;
import org.elasticsearch.xpack.core.action.util.QueryPage;
import org.elasticsearch.xpack.core.ml.inference.TrainedModelConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class GetTrainedModelsStatsAction extends ActionType<GetTrainedModelsStatsAction.Response> {

    public static final GetTrainedModelsStatsAction INSTANCE = new GetTrainedModelsStatsAction();
    public static final String NAME = "cluster:monitor/xpack/ml/inference/stats/get";

    public static final ParseField MODEL_ID = new ParseField("model_id");
    public static final ParseField PIPELINE_COUNT = new ParseField("pipeline_count");

    private GetTrainedModelsStatsAction() {
        super(NAME, GetTrainedModelsStatsAction.Response::new);
    }

    public static class Request extends AbstractGetResourcesRequest {

        public static final ParseField ALLOW_NO_MATCH = new ParseField("allow_no_match");

        public Request() {
            setAllowNoResources(true);
        }

        public Request(String id) {
            setResourceId(id);
            setAllowNoResources(true);
        }

        public Request(StreamInput in) throws IOException {
            super(in);
        }

        @Override
        public String getResourceIdField() {
            return TrainedModelConfig.MODEL_ID.getPreferredName();
        }

    }

    public static class RequestBuilder extends ActionRequestBuilder<Request, Response> {

        public RequestBuilder(ElasticsearchClient client, GetTrainedModelsStatsAction action) {
            super(client, action, new Request());
        }
    }

    public static class Response extends AbstractGetResourcesResponse<Response.TrainedModelStats> {

        public static class TrainedModelStats implements ToXContentObject, Writeable {
            private final String modelId;
            private final IngestStats ingestStats;
            private final int pipelineCount;

            private static final IngestStats EMPTY_INGEST_STATS = new IngestStats(new IngestStats.Stats(0, 0, 0, 0),
                Collections.emptyList(),
                Collections.emptyMap());

            public TrainedModelStats(String modelId, IngestStats ingestStats, int pipelineCount) {
                this.modelId = Objects.requireNonNull(modelId);
                this.ingestStats = ingestStats == null ? EMPTY_INGEST_STATS : ingestStats;
                if (pipelineCount < 0) {
                    throw new ElasticsearchException("[{}] must be a greater than or equal to 0", PIPELINE_COUNT.getPreferredName());
                }
                this.pipelineCount = pipelineCount;
            }

            public TrainedModelStats(StreamInput in) throws IOException {
                modelId = in.readString();
                ingestStats = new IngestStats(in);
                pipelineCount = in.readVInt();
            }

            public String getModelId() {
                return modelId;
            }

            @Override
            public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
                builder.startObject();
                builder.field(MODEL_ID.getPreferredName(), modelId);
                builder.field(PIPELINE_COUNT.getPreferredName(), pipelineCount);
                if (pipelineCount > 0) {
                    // Ingest stats is a fragment
                    ingestStats.toXContent(builder, params);
                }
                builder.endObject();
                return builder;
            }

            @Override
            public void writeTo(StreamOutput out) throws IOException {
                out.writeString(modelId);
                ingestStats.writeTo(out);
                out.writeVInt(pipelineCount);
            }

            @Override
            public int hashCode() {
                return Objects.hash(modelId, ingestStats, pipelineCount);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                TrainedModelStats other = (TrainedModelStats) obj;
                return Objects.equals(this.modelId, other.modelId)
                    && Objects.equals(this.ingestStats, other.ingestStats)
                    && Objects.equals(this.pipelineCount, other.pipelineCount);
            }
        }

        public static final ParseField RESULTS_FIELD = new ParseField("trained_model_stats");

        public Response(StreamInput in) throws IOException {
            super(in);
        }

        public Response(QueryPage<Response.TrainedModelStats> trainedModels) {
            super(trainedModels);
        }

        @Override
        protected Reader<Response.TrainedModelStats> getReader() {
            return Response.TrainedModelStats::new;
        }

        public static class Builder {

            private long totalModelCount;
            private Set<String> expandedIds;
            private Map<String, IngestStats> ingestStatsMap;

            public Builder setTotalModelCount(long totalModelCount) {
                this.totalModelCount = totalModelCount;
                return this;
            }

            public Builder setExpandedIds(Set<String> expandedIds) {
                this.expandedIds = expandedIds;
                return this;
            }

            public Set<String> getExpandedIds() {
                return this.expandedIds;
            }

            public Builder setIngestStatsByModelId(Map<String, IngestStats> ingestStatsByModelId) {
                this.ingestStatsMap = ingestStatsByModelId;
                return this;
            }

            public Response build() {
                List<TrainedModelStats> trainedModelStats = new ArrayList<>(expandedIds.size());
                expandedIds.forEach(id -> {
                    IngestStats ingestStats = ingestStatsMap.get(id);
                    trainedModelStats.add(new TrainedModelStats(id, ingestStats, ingestStats == null ?
                        0 :
                        ingestStats.getPipelineStats().size()));
                });
                trainedModelStats.sort(Comparator.comparing(TrainedModelStats::getModelId));
                return new Response(new QueryPage<>(trainedModelStats, totalModelCount, RESULTS_FIELD));
            }
        }
    }

}
