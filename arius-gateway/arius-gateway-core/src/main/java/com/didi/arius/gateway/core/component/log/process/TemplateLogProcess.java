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
 * @date 2021-09-23 4:18 下午
 */
public class TemplateLogProcess extends AbstractDslLogProcess {

    public static final String DSL_TEMPLATE_INDEX_NAME = "arius.dsl.template";

    public static final String PROJECT_ID_TEMPLATE = "projectIdDslTemplateMd5";

    public TemplateLogProcess(ESRestClientService esRestClientService, IndexTemplateService indexTemplateService) {
        super(esRestClientService, indexTemplateService);
    }

    @Override
    public void dealLog(List<JSONObject> records) {
        try {
            IndexTemplate indexTemplate = getTemplate(DSL_TEMPLATE_INDEX_NAME);
            if (null != indexTemplate) {
                ESClient esClient = esRestClientService.getClientStrict(indexTemplate.getMasterInfo().getCluster(), "TemplateLogProcess");
                ESBatchRequest esBatchRequest = new ESBatchRequest();
                esBatchRequest.setPipeline(indexTemplate.getIngestPipeline());
                records.stream().forEach(x ->
                        esBatchRequest.addNode(new BatchNode(BatchType.UPDATE, DSL_TEMPLATE_INDEX_NAME, TYPE,
                                x.getString(PROJECT_ID_TEMPLATE), x.toJSONString())));
                ESBatchResponse response = esClient.batch(esBatchRequest).get();
                if (response.getErrors()) {
                    bootLogger.warn("batch insert error [{}]", response.buildFailureMessage());
                }
            }
        } catch (Exception e) {
            bootLogger.error("insert dsl metric log error", e);
        }
    }
}