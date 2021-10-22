package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2018/9/19 下午5:36
 * @modified By
 *
 * join 后的gateway日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "GatewayJoinVO", description = "join 后的gateway日志")
public class GatewayJoinVO {
    /**
     * 请求id
     */
    @ApiModelProperty("请求id")
    private String requestId;
    /**
     * appid
     */
    @ApiModelProperty("appid")
    private Integer appid;
    /**
     * 索引名称
     */
    @ApiModelProperty("索引名称")
    private String indices;
    /**
     * type名称
     */
    @ApiModelProperty("type名称")
    private String typeName;
    /**
     * 查询命中索引信息json
     */
    @ApiModelProperty("查询命中索引信息json")
    private String index;
    /**
     * 查询语句
     */
    @ApiModelProperty("查询语句")
    private String dsl;
    /**
     * 查询模板
     */
    @ApiModelProperty("查询模板")
    private String dslTemplate;
    /**
     * 查询模板MD5
     */
    @ApiModelProperty("查询模板MD5")
    private String dslTemplateMd5;
    /**
     * 是否超时，"true"/"false"
     */
    @ApiModelProperty("是否超时，\"true\"/\"false\"")
    private String isTimedOut;
    /**
     * 查询语句类型
     */
    @ApiModelProperty("查询语句类型")
    private String dslType;
    /**
     * 查询方式,dsl/sql
     */
    @ApiModelProperty("查询方式,dsl/sql")
    private String searchType;
    /**
     * 查询es耗时
     */
    @ApiModelProperty("查询es耗时")
    private Long esCost;
    /**
     * 查询总耗时
     */
    @ApiModelProperty("查询总耗时")
    private Long totalCost;
    /**
     * 查询shard个数
     */
    @ApiModelProperty("查询shard个数")
    private Long totalShards;
    /**
     * 查询总命中数
     */
    @ApiModelProperty("查询总命中数")
    private Long totalHits;
    /**
     * 查询响应长度
     */
    @ApiModelProperty("查询响应长度")
    private Long responseLen;
    /**
     * 错误名称
     */
    @ApiModelProperty("错误名称")
    private String exceptionName;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private String ariusCreateTime;
    /**
     * timeStamp
     */
    @ApiModelProperty("timeStamp")
    private long timeStamp;
    /**
     * indiceSample
     */
    @ApiModelProperty("indiceSample")
    private String indiceSample;
    /**
     * 查询字段
     */
    @ApiModelProperty("查询字段")
    private Map<String, Long> selectFields;
    /**
     * 过滤字段
     */
    @ApiModelProperty("过滤字段")
    private Map<String, Long> whereFields;
    /**
     * 聚合字段
     */
    @ApiModelProperty("聚合字段")
    private Map<String, Long> groupByFields;
    /**
     * 排序字段
     */
    @ApiModelProperty("排序字段")
    private Map<String, Long> orderByFields;
    /**
     * 多type索引查询映射后的索引名称
     */
    @ApiModelProperty("多type索引查询映射后的索引名称")
    private String destIndexName;
    /**
     * 请求源ip
     */
    @ApiModelProperty("请求源ip")
    private String remoteAddr;
}
