package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import com.didiglobal.logi.elasticsearch.client.model.ESActionRequest;

public abstract class BaseTimeoutRequest<T extends ESActionRequest> extends ESActionRequest<T> {

    //默认超时时间
    protected String timeout = "30s";

    public T timeout(String timeout) {
        this.timeout = timeout;
        return (T) this;
    }

    protected BaseTimeoutRequest() {
    }

    protected BaseTimeoutRequest(ESActionRequest request) {
        super(request);
    }

}
