package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import com.didiglobal.logi.elasticsearch.client.model.ESActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public abstract class ESBroadcastTimeoutRequest<T extends ESBroadcastTimeoutRequest> extends BaseTimeoutRequest<T> implements IndicesRequest.Replaceable {
    protected String[] indices;
    private IndicesOptions indicesOptions = IndicesOptions.strictExpandOpenAndForbidClosed();

    protected ESBroadcastTimeoutRequest() {

    }

    protected ESBroadcastTimeoutRequest(ESActionRequest originalRequest) {
        super(originalRequest);
    }

    protected ESBroadcastTimeoutRequest(String[] indices) {
        this.indices = indices;
    }

    @Override
    public String[] indices() {
        return indices;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T indices(String... indices) {
        this.indices = indices;
        return (T) this;
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    @Override
    public IndicesOptions indicesOptions() {
        return indicesOptions;
    }

    @SuppressWarnings("unchecked")
    public final T indicesOptions(IndicesOptions indicesOptions) {
        this.indicesOptions = indicesOptions;
        return (T) this;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeStringArrayNullable(indices);
        indicesOptions.writeIndicesOptions(out);
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        indices = in.readStringArray();
        indicesOptions = IndicesOptions.readIndicesOptions(in);
    }
}
