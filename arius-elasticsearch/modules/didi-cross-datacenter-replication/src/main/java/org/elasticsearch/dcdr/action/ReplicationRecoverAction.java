package org.elasticsearch.dcdr.action;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.support.broadcast.BroadcastRequest;
import org.elasticsearch.action.support.broadcast.BroadcastResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * author weizijun
 * date：2019-10-24
 */
public class ReplicationRecoverAction extends
    ActionType<BroadcastResponse> {
    public static final ReplicationRecoverAction INSTANCE = new ReplicationRecoverAction();
    public static final String NAME = "indices:admin/dcdr/replication_recover";

    private ReplicationRecoverAction() {
        super(NAME, BroadcastResponse::new);
    }

    public static class Request extends BroadcastRequest<ReplicationRecoverAction.Request> {
        private int shardNum;

        @Override
        public ActionRequestValidationException validate() {
            return null;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeOptionalStringArray(indices);
            out.writeVInt(shardNum);
        }

        public Request() {

        }

        public Request(StreamInput in) throws IOException {
            super(in);
            indices = in.readOptionalStringArray();
            shardNum = in.readVInt();
        }

        @Override
        public String[] indices() {
            return indices;
        }

        public int getShardNum() {
            return shardNum;
        }

        public void setShardNum(int shardNum) {
            this.shardNum = shardNum;
        }
    }
}
