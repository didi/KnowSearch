package com.didi.arius.gateway.core.component.log.process;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.core.service.arius.IndexTemplateService;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.request.batch.BatchNode;
import com.didi.arius.gateway.elasticsearch.client.request.batch.BatchType;
import com.didi.arius.gateway.elasticsearch.client.request.batch.ESBatchRequest;
import com.didi.arius.gateway.elasticsearch.client.response.batch.ESBatchResponse;

import java.util.List;

/**
 * @author didi
 * @date 2021-09-23 3:32 下午
 */
public class MetricLogProcess extends AbstractDslLogProcess {

    public static final String DSL_INDEX_NAME = "arius.dsl.metrics";

    public MetricLogProcess(ESRestClientService esRestClientService, IndexTemplateService indexTemplateService) {
        super(esRestClientService, indexTemplateService);
    }

    @Override
    public void dealLog(List<JSONObject> records) {
        try {
            IndexTemplate indexTemplate = getTemplate(DSL_INDEX_NAME);
            if (null != indexTemplate) {
                ESClient esClient = esRestClientService.getClientStrict(indexTemplate.getMasterInfo().getCluster(), "MetricLogProcess");
                ESBatchRequest esBatchRequest = new ESBatchRequest();
                esBatchRequest.setPipeline(indexTemplate.getIngestPipeline());
                records.stream().forEach(x ->
                        esBatchRequest.addNode(new BatchNode(BatchType.INDEX, DSL_INDEX_NAME, TYPE, null, x.toJSONString())));
                ESBatchResponse response = esClient.batch(esBatchRequest).get();
                if (response.getErrors()) {
                    bootLogger.warn("batch insert error [{}]", response.buildFailureMessage());
                }
            }
        } catch (Exception e) {
            bootLogger.error("insert dsl template log error", e);
        }
    }
}
