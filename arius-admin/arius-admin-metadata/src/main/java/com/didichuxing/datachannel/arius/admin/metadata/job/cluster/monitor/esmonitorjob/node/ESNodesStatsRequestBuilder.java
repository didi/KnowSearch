package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

import static com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node.ESNodesStatsRequest.*;

public class ESNodesStatsRequestBuilder extends ActionRequestBuilder<ESNodesStatsRequest, ESNodesStatsResponse, ESNodesStatsRequestBuilder> {

    public ESNodesStatsRequestBuilder(ElasticsearchClient client, ESNodesStatsAction action) {
        super(client, action, new ESNodesStatsRequest());
    }

    public final ESNodesStatsRequestBuilder setNodesIds(String... nodesIds) {
        request.nodesIds(nodesIds);
        return this;
    }

    public ESNodesStatsRequestBuilder all() {
        request.all();
        return this;
    }

    public ESNodesStatsRequestBuilder clear() {
        request.clear();
        return this;
    }

    public ESNodesStatsRequestBuilder level(String level) {
        request.level(level);
        return this;
    }

    public ESNodesStatsRequestBuilder timeout(String timeout) {
        request.timeout(timeout);
        return this;
    }

    public ESNodesStatsRequestBuilder setIndices(boolean indices) {
        request.flag(INDICES, indices);
        return this;
    }

    public ESNodesStatsRequestBuilder setBreaker(boolean breaker) {
        request.flag(BREAKERS, breaker);
        return this;
    }

    public ESNodesStatsRequestBuilder setScript(boolean script) {
        request.flag(SCRIPT, script);
        return this;
    }

    public ESNodesStatsRequestBuilder setOs(boolean os) {
        request.flag(OS, os);
        return this;
    }

    public ESNodesStatsRequestBuilder setProcess(boolean process) {
        request.flag(PROCESS, process);
        return this;
    }

    public ESNodesStatsRequestBuilder setJvm(boolean jvm) {
        request.flag(JVM, jvm);
        return this;
    }

    public ESNodesStatsRequestBuilder setThreadPool(boolean threadPool) {
        request.flag(THREAD_POOL, threadPool);
        return this;
    }

    public ESNodesStatsRequestBuilder setFs(boolean fs) {
        request.flag(FS, fs);
        return this;
    }

    public ESNodesStatsRequestBuilder setTransport(boolean transport) {
        request.flag(TRANSPORT, transport);
        return this;
    }

    public ESNodesStatsRequestBuilder setHttp(boolean http) {
        request.flag(HTTP, http);
        return this;
    }
}
