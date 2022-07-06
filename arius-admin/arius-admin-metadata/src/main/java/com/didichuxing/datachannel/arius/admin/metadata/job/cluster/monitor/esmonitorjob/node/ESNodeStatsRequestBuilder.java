package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

import static com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node.ESNodeStatsRequest.*;

public class ESNodeStatsRequestBuilder extends ActionRequestBuilder<ESNodeStatsRequest, ESNodeStatsResponse, ESNodeStatsRequestBuilder> {

    public ESNodeStatsRequestBuilder(ElasticsearchClient client, ESNodeStatsAction action) {
        super(client, action, new ESNodeStatsRequest());
    }

    public final ESNodeStatsRequestBuilder setNodesIds(String... nodesIds) {
        request.nodesIds(nodesIds);
        return this;
    }

    public ESNodeStatsRequestBuilder all() {
        request.all();
        return this;
    }

    public ESNodeStatsRequestBuilder clear() {
        request.clear();
        return this;
    }

    public ESNodeStatsRequestBuilder level(String level) {
        request.level(level);
        return this;
    }

    public ESNodeStatsRequestBuilder timeout(String timeout) {
        request.timeout(timeout);
        return this;
    }

    public ESNodeStatsRequestBuilder setIndices(boolean indices) {
        request.flag(INDICES, indices);
        return this;
    }

    public ESNodeStatsRequestBuilder setBreaker(boolean breaker) {
        request.flag(BREAKERS, breaker);
        return this;
    }

    public ESNodeStatsRequestBuilder setScript(boolean script) {
        request.flag(SCRIPT, script);
        return this;
    }

    public ESNodeStatsRequestBuilder setOs(boolean os) {
        request.flag(OS, os);
        return this;
    }

    public ESNodeStatsRequestBuilder setProcess(boolean process) {
        request.flag(PROCESS, process);
        return this;
    }

    public ESNodeStatsRequestBuilder setJvm(boolean jvm) {
        request.flag(JVM, jvm);
        return this;
    }

    public ESNodeStatsRequestBuilder setThreadPool(boolean threadPool) {
        request.flag(THREAD_POOL, threadPool);
        return this;
    }

    public ESNodeStatsRequestBuilder setFs(boolean fs) {
        request.flag(FS, fs);
        return this;
    }

    public ESNodeStatsRequestBuilder setTransport(boolean transport) {
        request.flag(TRANSPORT, transport);
        return this;
    }

    public ESNodeStatsRequestBuilder setHttp(boolean http) {
        request.flag(HTTP, http);
        return this;
    }
}
