package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.ES_OPERATE_TIMEOUT;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ESPipelineProcessor;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.ingest.Pipeline;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.ingest.ESDeletePipelineResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.ingest.ESGetPipelineResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.ingest.ESPutPipelineResponse;
import com.google.common.collect.Lists;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Repository
public class ESPipelineDAO extends BaseESDAO {

    private static final ILog  LOGGER                            = LogFactory.getLog(ESPipelineDAO.class);

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
    public static final String FLINK_DATE_TIME                   = "_FLINK_DATA_TIME";
    public static final String TEMPLATE_FLINK_DATE_TIME          = "logTime";

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
        pipeline.setProcessors(buildProcessors(dateField, dateFieldFormat, dateFormat, expireDay, version, rateLimit,
            idField, routingField, pipeline.getProcessors()));

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
            return JSON.parseObject(JSON.toJSONString(pipeline.getProcessors().get(0)), ESPipelineProcessor.class);
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
                                             Integer expireDay, Integer indexVersion, Integer rateLimit, String idField,
                                             String routingField, List<JSONObject> oldProcessors) {

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
            // 日志类型设置pipeline时，时间字段设置为_FLINK_DATA_TIME
            if (TEMPLATE_FLINK_DATE_TIME.equals(dateField)) {
                dateField = FLINK_DATE_TIME;
                dateFieldFormat = MS_TIME_FIELD_ES_FORMAT;
            }
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
