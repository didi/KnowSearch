package com.didichuxing.datachannel.arius.plugin.appendlucene;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

public class AppendLuceneResponse extends ActionResponse implements ToXContentObject {
    public long deleteCount;

    public AppendLuceneResponse() {}

    public AppendLuceneResponse(StreamInput in) throws IOException {
        super(in);
        deleteCount = in.readLong();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeLong(deleteCount);
    }

    private static final String APPEND_LUCENE_OK = "APPEND_LUCENE_OK";

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field("msg", APPEND_LUCENE_OK);
        builder.field("delete_count", deleteCount);
        builder.endObject();
        return builder;
    }
}
