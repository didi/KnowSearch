package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2018/9/18 下午5:24
 * @modified By
 *
 * 查询模板一分钟聚合指标统计信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DslMetricsVO", description = "查询模板一分钟聚合指标统计信息")
public class DslMetricsVO {

    @ApiModelProperty("创建时间")
    private String  ariusCreateTime;

    @ApiModelProperty("日期时间")
    private String  logTime;

    @ApiModelProperty("sink写入时间")
    private Long    sinkTime;

    @ApiModelProperty("flink写入时间")
    private String  flinkTime;

    @ApiModelProperty("响应平均长度")
    private Double  responseLenAvg;

    @ApiModelProperty("请求类型 http/tcp")
    private String  requestType;

    @ApiModelProperty("查询类型 dsl/sql")
    private String  searchType;

    @ApiModelProperty("查询次数")
    private Long    searchCount;

    @ApiModelProperty("查询es平均耗时")
    private Double  esCostAvg;

    @ApiModelProperty("查询语句平均长度")
    private Double  dslLenAvg;

    @ApiModelProperty("查询平均命中记录数")
    private Double  totalHitsAvg;

    @ApiModelProperty("查询成功平均shard数")
    private Double  successfulShardsAvg;

    @ApiModelProperty("查询平均总shard数")
    private Double  totalShardsAvg;

    @ApiModelProperty("查询索引示例")
    private String  indiceSample;

    @ApiModelProperty("查询模板")
    private String  dslTemplate;

    @ApiModelProperty("记录生成时间戳")
    private Long    timeStamp;

    @ApiModelProperty("dsl语句类型 normal/agg等")
    private String  dslType;

    @ApiModelProperty("查询索引名称，去重后的")
    private String  indices;

    @ApiModelProperty("查询模板")
    private String  dslTemplateMd5;

    @ApiModelProperty("查询平均总耗时")
    private Double  totalCostAvg;

    @ApiModelProperty("查询平均失败shard数")
    private Double  failedShardsAvg;

    @ApiModelProperty("appid")
    private Integer appid;

    @ApiModelProperty("查询语句")
    private String  dsl;

    @ApiModelProperty("gateway处理平均耗时")
    private Double  beforeCostAvg;

    @ApiModelProperty("dsl查询限流值")
    private Double  queryLimit;
}
