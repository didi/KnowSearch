package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ESPipelineProcessor;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.request.ingest.Pipeline;
import com.didiglobal.logi.elasticsearch.client.response.ingest.ESDeletePipelineResponse;
import com.didiglobal.logi.elasticsearch.client.response.ingest.ESGetPipelineResponse;
import com.didiglobal.logi.elasticsearch.client.response.ingest.ESPutPipelineResponse;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.ES_OPERATE_TIMEOUT;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Repository
public class ESPipelineDAO extends BaseESDAO {

    public static final String INDEX_TEMPLATE_PROCESSOR          = "index_template";
    public static final String THROTTLE_PROCESSOR                = "throttle";
    public static final String DATE_FIELD                        = "field";
    public static final String DATE_FIELD_FORMAT                 = "field_format";
    public static final String INDEX_NAME_FORMAT                 = "index_name_format";
    public static final String EXPIRE_DAY                        = "expire_day";
    public static final String INDEX_VERSION                     = "index_version";
    public static final String ID_FIELD                          = "id_field";
    public static final String ROUTING_FIELD                     = "routing_field";
    public static final String RATE_LIMIT                        = "rate_limit";
    public static final String MS_TIME_FIELD_ES_FORMAT           = "UNIX_MS";
    public static final String SECOND_TIME_FIELD_ES_FORMAT       = "UNIX";
    public static final String MS_TIME_FIELD_PLATFORM_FORMAT     = "epoch_millis";
    public static final String SECOND_TIME_FIELD_PLATFORM_FORMAT = "epoch_seconds";
    public static final int    FILE_BEATS_PROCESSOR_INDEX        = 0;
    public static final Set<String>        FILE_BEATS_PIPELINE_ID_SET        = new HashSet<>();
    public static final List<JSONObject>   FILE_BEATS_PROCESSOR              = new ArrayList<>();

    static {
        FILE_BEATS_PROCESSOR.add(JSON.parseObject("{\"grok\":{\"field\":\"message\",\"patterns\":[\"%{GREEDYDATA}] {%{GREEDYDATA:message}\"]}}"));
        FILE_BEATS_PROCESSOR.add(JSON.parseObject("{\"set\":{\"field\":\"message\",\"value\":\"{ {{{message}}}\"}}"));
        FILE_BEATS_PROCESSOR.add(JSON.parseObject("{\"json\":{\"field\":\"message\",\"add_to_root\":true}}"));
        FILE_BEATS_PROCESSOR.add(JSON.parseObject("{\"remove\":{\"field\":[\"message\",\"@timestamp\",\"flag\"],\"ignore_missing\":true}}"));

        FILE_BEATS_PIPELINE_ID_SET.add("arius.gateway.join");
        FILE_BEATS_PIPELINE_ID_SET.add("cn_arius_gateway_metrics");
    }

    /**
     * 创建
     * @param cluster 集群
     * @param pipelineId id
     * @param dateField 时间字段
     * @param dateFieldFormat 时间字段格式
     * @param dateFormat 时间后缀
     * @param expireDay 过期时间
     * @param rateLimit 限流
     * @return true/false
     */
    public boolean save(String cluster, String pipelineId, String dateField, String dateFieldFormat, String dateFormat,
                        Integer expireDay, Integer rateLimit, Integer version, String idField, String routingField) {
        LOGGER.info("class=ESPipelineDAO||method=save||cluster={}||pipelineId={}||dateField={}||" +
                        "dateFieldFormat={}||dateFormat={}||expireDay={}||rateLimit={}||version={}||idField={}||" +
                        "routingField={}",
                cluster, pipelineId, dateField, dateFieldFormat, dateFormat,
                expireDay, rateLimit, version, idField, routingField);

        ESClient client = esOpClient.getESClient(cluster);

        Pipeline pipeline = null;

        try {
            pipeline = getPipeLine(cluster, pipelineId);
        } catch (Exception e) {
            LOGGER.warn("class=ESPipelineDAO||method=save||cluster={}||pipelineId={}||msg=failed to get pipeline||exception={}",
                    cluster, pipelineId, e);
        }

        if (pipeline == null) {
            pipeline = new Pipeline();
        }

        pipeline.setDescription(pipelineId);
        pipeline.setProcessors(buildProcessors(dateField, dateFieldFormat, dateFormat, expireDay, version, rateLimit, pipeline.getProcessors()));

        if (FILE_BEATS_PIPELINE_ID_SET.contains(pipelineId)) {
            pipeline.getProcessors().addAll(FILE_BEATS_PROCESSOR_INDEX, FILE_BEATS_PROCESSOR);
        }

        ESPutPipelineResponse response = client.admin().indices().preparePutPipeline().setPipelineId(pipelineId)
            .setPipeline(pipeline).execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();

    }

    /**
     * 删除pipeline
     * @param cluster 集群
     * @param pipelineId pipelineId
     * @return true/false
     */
    public boolean delete(String cluster, String pipelineId) {
        ESClient client = esOpClient.getESClient(cluster);

        ESDeletePipelineResponse response = client.admin().indices().prepareDeletePipeline().setPipelineId(pipelineId)
            .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getAcknowledged();
    }

    /**
     * 获取pipeline
     * @param cluster 集群
     * @param pipelineId pipeline
     * @return processor
     */
    public ESPipelineProcessor get(String cluster, String pipelineId) {
        try {
            Pipeline pipeline = getPipeLine(cluster, pipelineId);
            if (pipeline == null) {
                return null;
            }
            JSONObject process = pipeline.getProcessors().get(0);
            if (FILE_BEATS_PIPELINE_ID_SET.contains(pipelineId)) {
                process = pipeline.getProcessors().get(4);
            }
            return JSON.parseObject(JSON.toJSONString(process), ESPipelineProcessor.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**************************************** private method ****************************************************/

    private Pipeline getPipeLine(String cluster, String pipelineId) {
        ESClient client = esOpClient.getESClient(cluster);

        ESGetPipelineResponse response = client.admin().indices().prepareGetPipeline().setPipelineId(pipelineId)
            .execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        Map<String, Pipeline> pipelineMap = response.getPipelineMap();
        if (pipelineMap == null || !pipelineMap.containsKey(pipelineId)) {
            return null;
        }

        return pipelineMap.get(pipelineId);
    }

    private List<JSONObject> buildProcessors(String dateField, String dateFieldFormat, String indexNameFormat,
                                             Integer expireDay, Integer indexVersion, Integer rateLimit, List<JSONObject> oldProcessors) {

        if (MS_TIME_FIELD_PLATFORM_FORMAT.equals(dateFieldFormat)) {
            dateFieldFormat = MS_TIME_FIELD_ES_FORMAT;
        } else if (SECOND_TIME_FIELD_PLATFORM_FORMAT.equals(dateFieldFormat)) {
            dateFieldFormat = SECOND_TIME_FIELD_ES_FORMAT;
        }

        if (indexNameFormat == null) {
            indexNameFormat = "";
        }

        JSONObject pipelineProcessors = CollectionUtils.isNotEmpty(oldProcessors) ? oldProcessors.get(0)
            : new JSONObject();

        pipelineProcessors.put(INDEX_TEMPLATE_PROCESSOR, new JSONObject());
        pipelineProcessors.put(THROTTLE_PROCESSOR, new JSONObject());

        JSONObject indexTemplateProcessor = pipelineProcessors.getJSONObject(INDEX_TEMPLATE_PROCESSOR);
        // 分区创建的
        if (StringUtils.isNoneBlank(indexNameFormat)) {
            indexTemplateProcessor.put(DATE_FIELD, dateField);
            if (StringUtils.isNotBlank(dateFieldFormat)) {
                indexTemplateProcessor.put(DATE_FIELD_FORMAT, dateFieldFormat);
            }
        }
        indexTemplateProcessor.put(INDEX_NAME_FORMAT, indexNameFormat);
        indexTemplateProcessor.put(EXPIRE_DAY, expireDay);
        indexTemplateProcessor.put(INDEX_VERSION, indexVersion);

        JSONObject throttle = pipelineProcessors.getJSONObject(THROTTLE_PROCESSOR);
        throttle.put(RATE_LIMIT, rateLimit);

        return Lists.newArrayList(pipelineProcessors);
    }

}
