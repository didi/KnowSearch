package com.didi.cloud.fastdump.core.service.metrics;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.didi.cloud.fastdump.common.bean.metrics.IndexMoveMetrics;
import com.didi.cloud.fastdump.common.client.es.ESRestClient;
import com.didi.cloud.fastdump.common.utils.IndexNameUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Created by linyunan on 2022/11/22
 */
@Service
public class ESMetricsServiceImpl implements ESMetricsService {
    protected static final Logger                                      LOGGER               = LoggerFactory
        .getLogger(ESMetricsServiceImpl.class);
    @Value("${es.metrics.cluster.http.connect.address}")
    private String                                                     metricClusterHttpAddress;

    @Value("${es.metrics.cluster.user.name}")
    private String                                                     metricClusterUserName;

    @Value("${es.metrics.cluster.password}")
    private String                                                     metricClusterPassword;

    @Value("${es.metrics.template.name}")
    private String                                                     metricsTemplateName;

    private static final Cache<String/*clusterAddress*/, ESRestClient> ES_REST_CLIENT_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Override
    public void save(IndexMoveMetrics indexMoveMetrics) {
        try {
            ESRestClient esRestClient = ES_REST_CLIENT_CACHE.get(metricClusterHttpAddress,
                    () -> new ESRestClient(metricClusterHttpAddress, metricClusterUserName, metricClusterPassword));
            
            String indexName = IndexNameUtils.genCurrentDailyIndexName(metricsTemplateName);
            
            String metricsStr = buildMetricsStr(indexMoveMetrics, indexName);
            esRestClient.syncRetryBulkWrite(metricsStr);
        } catch (Exception e) {
            LOGGER.error("class=ESMetricsServiceImpl||method=save||errMsg={}", e.getMessage());
        }
    }

    private String buildMetricsStr(IndexMoveMetrics indexMoveMetrics, String index) {
        StringBuilder metricsDocumentBuilder = new StringBuilder();

        // 1. 构建二级文档信息
        JSONObject childJsonObj = new JSONObject();
        childJsonObj.put("_index", index);

        // 2. 构建一级文档信息, 且把二级文档信息添加入内
        JSONObject parentJsonObj = new JSONObject();
        parentJsonObj.put("create", childJsonObj);

        String source = JSONObject.toJSONString(indexMoveMetrics);
        if (StringUtils.isBlank(source)) { return null;}

        metricsDocumentBuilder.append(parentJsonObj.toJSONString()).append("\n");
        metricsDocumentBuilder.append(source).append("\n");
        return metricsDocumentBuilder.toString();
    }
}
