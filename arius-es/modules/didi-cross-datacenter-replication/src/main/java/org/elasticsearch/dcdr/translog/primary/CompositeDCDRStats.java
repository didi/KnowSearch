package org.elasticsearch.dcdr.translog.primary;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * author weizijun
 * date：2019-09-25
 */
public class CompositeDCDRStats implements Writeable, ToXContentFragment {

    private String primaryIndex;

    private int shardId;

    List<DCDRStats> dcdrStatsList = new ArrayList<>();

    public CompositeDCDRStats() {}

    public CompositeDCDRStats(StreamInput in) throws IOException {
        readFrom(in);
    }

    public String getPrimaryIndex() {
        return primaryIndex;
    }

    public void setPrimaryIndex(String primaryIndex) {
        this.primaryIndex = primaryIndex;
    }

    public int getShardId() {
        return shardId;
    }

    public void setShardId(int shardId) {
        this.shardId = shardId;
    }

    public List<DCDRStats> getDcdrStatsList() {
        return dcdrStatsList;
    }

    public void setDcdrStatsList(List<DCDRStats> dcdrStatsList) {
        this.dcdrStatsList = dcdrStatsList;
    }

    public void readFrom(StreamInput in) throws IOException {
        this.primaryIndex = in.readString();
        this.shardId = in.readVInt();
        this.dcdrStatsList = in.readList(DCDRStats::new);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(primaryIndex);
        out.writeVInt(shardId);
        out.writeList(dcdrStatsList);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startArray(String.valueOf(shardId));
        for (DCDRStats stats : dcdrStatsList) {
            stats.toXContent(builder, params);
        }
        builder.endArray();

        return builder;
    }
}
