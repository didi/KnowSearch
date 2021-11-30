package com.didi.arius.gateway.common.metadata;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metrics.log.DslMetricHelper;
import com.didi.arius.gateway.common.utils.Convert;
import lombok.Data;
import org.elasticsearch.rest.RestRequest;

import java.util.Set;

import static com.didi.arius.gateway.elasticsearch.client.utils.LogUtils.KIBANA_LOG;

@Data
public class JoinLogContext {
    private Boolean queryRequest = true;
    private String idc;
    private String exceptionName;
    private String stack;
    private int appid;
    private String ariusType;
    private String requestId;
    private AppDetail.RequestType requestType;
    private RestRequest.Method method;
    private String group;
    private String gatewayNode;
    private String clusterId;
    private String user;
    private String xUserName;
    private String clientVersion;
    private String clientNode;
    private String uri;
    private String queryString;
    private String remoteAddr;
    private int dslLen;
    private String dsl;
    private Set<String> dslTag;
    private int responseLen;
    private int status;
    private String scrollId;
    private long esCost;
    private int totalShards;
    private int failedShards;
    private long totalHits;
    private Boolean isTimedOut;
    private String sourceIndexNames;
    private String destIndexName;
    private String sourceTemplateName;
    private String destTemplateName;
    private String dslTemplate;
    private String searchType;
    private String dslType;
    private String dslTemplateMd5;
    private String selectFields;
    private String whereFields;
    private String groupByFields;
    private String sortByFields;
    private int aggsLevel;
    private String index;
    private String indices;
    private String typeName;
    private String clusterName;
    private int logicId;
    private long beforeCost;
    private long paramCost;
    private long indexTemplateCost;
    private long getClientCost;
    private long preProcessCost;
    private long searchCost;
    private long totalCost;
    private long internalCost;
    private long timeStamp;
    private long sinkTime;

    public JoinLogContext() {
        this.gatewayNode = Convert.getHostName();
    }

    @Override
    public String toString() {
        if (uri != null && (uri.startsWith("/."))) {
            return KIBANA_LOG;
        }
        String res = JSON.toJSONString(this);
        DslMetricHelper.putDslLog(res);
        return res;
    }
}