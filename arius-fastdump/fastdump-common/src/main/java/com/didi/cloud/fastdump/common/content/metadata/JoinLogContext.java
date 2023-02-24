package com.didi.cloud.fastdump.common.content.metadata;

import java.util.Set;

import org.elasticsearch.rest.RestRequest;

import com.didi.cloud.fastdump.common.utils.HostUtil;

import lombok.Data;

@Data
public class JoinLogContext {
    private Boolean            queryRequest = true;
    private String             idc;
    private String             exceptionName;
    private String             stack;
    private int                appid;
    private int                projectId;
    private String             ariusType;
    private String             traceid;
    private String             requestId;
    private RestRequest.Method method;
    private String             group;
    private String             gatewayNode;
    private String             clusterId;
    private String             user;
    private String             xUserName;
    private String             clientVersion;
    private String             clientNode;
    private String             uri;
    private String             queryString;
    private String             remoteAddr;
    private int                dslLen;
    private String             dsl;
    private Set<String>        dslTag;
    private int                responseLen;
    private int                status;
    private String             scrollId;
    private long               esCost;
    private int                totalShards;
    private int                failedShards;
    private long               totalHits;
    private Boolean            isTimedOut;
    private String             sourceIndexNames;
    private String             destIndexName;
    private String             sourceTemplateName;
    private String             destTemplateName;
    private String             dslTemplate;
    private String             searchType;
    private String             dslType;
    private String             dslTemplateMd5;
    private String             selectFields;
    private String             whereFields;
    private String             groupByFields;
    private String             sortByFields;
    private int                aggsLevel;
    private String             index;
    private String             indices;
    private String             typeName;
    private String             clusterName;
    private int                logicId;
    private long               beforeCost;
    private long               paramCost;
    private long               indexTemplateCost;
    private long               getClientCost;
    private long               preProcessCost;
    private long               searchCost;
    private long               totalCost;
    private long               internalCost;
    private long               timeStamp;
    private long               sinkTime;

    public JoinLogContext() {
        this.gatewayNode = HostUtil.getHostName();
    }
}