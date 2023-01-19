package org.elasticsearch.dcdr.action;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.support.broadcast.BroadcastRequest;
import org.elasticsearch.action.support.broadcast.BroadcastResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.dcdr.DCDRShardInfo;

import java.io.IOException;

/**
 * author weizijun
 * date：2019-12-09
 */
public class FetchShardInfoAction extends
    ActionType<FetchShardInfoAction.Response> {
    public static final FetchShardInfoAction INSTANCE = new FetchShardInfoAction();
    public static final String NAME = "indices:admin/dcdr/fetch_shard_info";

    private FetchShardInfoAction() {
        super(NAME, Response::new);
    }

    public static class Request extends BroadcastRequest<Request> {

        public int getShardNum() {
            return shardNum;
        }

        public Request(StreamInput in) throws IOException {
            super(in);
            shardNum = in.readVInt();
        }

        public Request(String index, int shardNum) {
            indices(index);
            this.shardNum = shardNum;
        }

        private int shardNum;

        @Override
        public ActionRequestValidationException validate() {
            return null;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeVInt(shardNum);
        }
    }

    public static class Response extends BroadcastResponse {
        public DCDRShardInfo getDcdrShardInfo() {
            return dcdrShardInfo;
        }

        private DCDRShardInfo dcdrShardInfo;

        private Response() {}

        public Response(DCDRShardInfo dcdrShardInfo) {
            this.dcdrShardInfo = dcdrShardInfo;
        }

        public Response(StreamInput in) throws IOException {
            super(in);
            this.dcdrShardInfo = new DCDRShardInfo(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            dcdrShardInfo.writeTo(out);
        }

        @Override
        protected void addCustomXContentFields(XContentBuilder builder, Params params) throws IOException {
            builder.startObject(Fields.SHARD_INFO);
            dcdrShardInfo.toXContent(builder, params);
            builder.endObject();
        }

        static final class Fields {
            static final String SHARD_INFO = "shard_info";
        }
    }
}
