package com.didi.arius.gateway.common.metrics.log;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2021-09-16 10:46 下午
 */
@Data
@NoArgsConstructor
public class DslLogEntity {
    private int dslLen;
    private int responseLen;
    private long beforeCost;
    private long esCost;
    private long totalCost;
    private int successfulShards;
    private int totalShards;
    private int failedShards;
    private long totalHits;
    private int appid;
    private int projectId;
    private String dslTemplate;
    private String dslTemplateMd5;
    private String appidDslTemplateMd5;
    private String projectIdDslTemplateMd5;
    private long timeStamp;
    private String dsl;
    private String indices;
    private String indiceSample;
    private String requestType;
    private String searchType;
    private String dslType;
    private boolean isFromUserConsole;
    private String version;
    private String dslLevel;
    private String dslTag;
    private long searchCount = 1;
    private String ariusType;
    private String gatewayNode;
    private boolean queryRequest;

    //平均值
    private double dslLenAvg;
    private double responseLenAvg;
    private double beforeCostAvg;
    private double esCostAvg;
    private double totalCostAvg;
    private double successfulShardsAvg;
    private double totalShardsAvg;
    private double failedShardsAvg;
    private double totalHitsAvg;

}