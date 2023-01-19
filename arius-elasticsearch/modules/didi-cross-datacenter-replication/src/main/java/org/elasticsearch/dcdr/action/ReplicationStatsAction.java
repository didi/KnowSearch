package org.elasticsearch.dcdr.action;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.broadcast.BroadcastRequest;
import org.elasticsearch.action.support.broadcast.BroadcastResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.dcdr.translog.primary.CompositeDCDRStats;

/**
 * author weizijun
 * date：2019-09-25
 */
public class ReplicationStatsAction extends
    ActionType<ReplicationStatsAction.Response> {
    public static final ReplicationStatsAction INSTANCE = new ReplicationStatsAction();
    public static final String NAME = "indices:admin/dcdr/replication_stats";

    private ReplicationStatsAction() {
        super(NAME, ReplicationStatsAction.Response::new);
    }

    public static class Request extends BroadcastRequest<Request> {

        private String[] indices;

        public Request() {

        }

        public Request(StreamInput in) throws IOException {
            super(in);
            indices = in.readOptionalStringArray();
        }

        @Override
        public ActionRequestValidationException validate() {
            return null;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeOptionalStringArray(indices);
        }

        @Override
        public String[] indices() {
            return indices;
        }

        public void setIndices(final String[] indices) {
            this.indices = indices;
        }

        @Override
        public IndicesOptions indicesOptions() {
            return IndicesOptions.strictExpand();
        }

    }

    public static class Response extends BroadcastResponse {

        private List<CompositeDCDRStats> dcdrStats;

        public Response(List<CompositeDCDRStats> dcdrStats) {
            this.dcdrStats = dcdrStats;
        }

        public Response(StreamInput in) throws IOException {
            super(in);
            dcdrStats = in.readList(CompositeDCDRStats::new);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeList(dcdrStats);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            // sort by index name, then shard ID
            final Map<String, Map<Integer, CompositeDCDRStats>> responsesByIndex = new TreeMap<>();
            for (final CompositeDCDRStats statsResponse : dcdrStats) {
                responsesByIndex.computeIfAbsent(
                    statsResponse.getPrimaryIndex(),
                    k -> new TreeMap<>()
                ).put(statsResponse.getShardId(), statsResponse);
            }
            builder.startObject();
            {
                for (final Map.Entry<String, Map<Integer, CompositeDCDRStats>> indexEntry : responsesByIndex.entrySet()) {
                    builder.startObject(indexEntry.getKey());
                    {
                        for (final Map.Entry<Integer, CompositeDCDRStats> shardEntry : indexEntry.getValue().entrySet()) {
                            shardEntry.getValue().toXContent(builder, params);
                        }
                    }
                    builder.endObject();
                }
            }
            builder.endObject();
            return builder;
        }

        public List<CompositeDCDRStats> getDcdrStats() {
            return dcdrStats;
        }
    }
}
