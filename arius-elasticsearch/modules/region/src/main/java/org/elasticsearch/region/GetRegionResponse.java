package org.elasticsearch.region;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GetRegionResponse extends ActionResponse implements ToXContentObject {
    private Map<String, List<String>> seeds;

    public GetRegionResponse(Map<String, List<String>> seeds) {
        this.seeds = seeds;
    }

    public GetRegionResponse(StreamInput in) throws IOException {
        super(in);
        this.seeds = in.readMapOfLists(StreamInput::readString, StreamInput::readString);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeMapOfLists(seeds, StreamOutput::writeString, StreamOutput::writeString);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        if (seeds != null) {
            for (Map.Entry<String, List<String>> entry : seeds.entrySet()) {
                builder.array(entry.getKey(), entry.getValue());
            }
        }
        builder.endObject();
        return builder;
    }
}
