package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

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
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private String ariusCreateTime;
    /**
     * 日期时间
     */
    @ApiModelProperty("日期时间")
    private String logTime;
    /**
     * sink写入时间
     */
    @ApiModelProperty("sink写入时间")
    private Long sinkTime;
    /**
     * flink写入时间
     */
    @ApiModelProperty("flink写入时间")
    private String flinkTime;
    /**
     * 响应平均长度
     */
    @ApiModelProperty("响应平均长度")
    private Double responseLenAvg;
    /**
     * 请求类型 http/tcp
     */
    @ApiModelProperty("请求类型 http/tcp")
    private String requestType;
    /**
     * 查询类型 dsl/sql
     */
    @ApiModelProperty("查询类型 dsl/sql")
    private String searchType;
    /**
     * 查询次数
     */
    @ApiModelProperty("查询次数")
    private Long searchCount;
    /**
     * 查询es平均耗时
     */
    @ApiModelProperty("查询es平均耗时")
    private Double esCostAvg;
    /**
     * 查询语句平均长度
     */
    @ApiModelProperty("查询语句平均长度")
    private Double dslLenAvg;
    /**
     * 查询平均命中记录数
     */
    @ApiModelProperty("查询平均命中记录数")
    private Double totalHitsAvg;
    /**
     * 查询成功平均shard数
     */
    @ApiModelProperty("查询成功平均shard数")
    private Double successfulShardsAvg;
    /**
     * 查询平均总shard数
     */
    @ApiModelProperty("查询平均总shard数")
    private Double totalShardsAvg;
    /**
     * 查询索引示例
     */
    @ApiModelProperty("查询索引示例")
    private String indiceSample;
    /**
     * 查询模板
     */
    @ApiModelProperty("查询模板")
    private String dslTemplate;
    /**
     * 记录生成时间戳
     */
    @ApiModelProperty("记录生成时间戳")
    private Long timeStamp;
    /**
     * dsl语句类型 normal/agg等
     */
    @ApiModelProperty("dsl语句类型 normal/agg等")
    private String dslType;
    /**
     * 查询索引名称，去重后的
     */
    @ApiModelProperty("查询索引名称，去重后的")
    private String indices;
    /**
     * 查询模板
     */
    @ApiModelProperty("查询模板")
    private String dslTemplateMd5;
    /**
     * 查询平均总耗时
     */
    @ApiModelProperty("查询平均总耗时")
    private Double totalCostAvg;
    /**
     * 查询平均失败shard数
     */
    @ApiModelProperty("查询平均失败shard数")
    private Double failedShardsAvg;
    /**
     * appid
     */
    @ApiModelProperty("appid")
    private Integer appid;
    /**
     * 查询语句
     */
    @ApiModelProperty("查询语句")
    private String dsl;
    /**
     * gateway处理平均耗时
     */
    @ApiModelProperty("gateway处理平均耗时")
    private Double beforeCostAvg;
    /**
     * dsl查询限流值
     */
    @ApiModelProperty("dsl查询限流值")
    private Double   queryLimit;
}
