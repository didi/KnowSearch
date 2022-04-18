package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

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

    @ApiModelProperty("请求path")
    private String uri;

    @ApiModelProperty("请求id")
    private String requestId;

    @ApiModelProperty("appid")
    private Integer appid;

    @ApiModelProperty("索引名称")
    private String indices;

    @ApiModelProperty("type名称")
    private String typeName;

    @ApiModelProperty("查询命中索引信息json")
    private String index;

    @ApiModelProperty("查询语句")
    private String dsl;

    @ApiModelProperty("查询模板")
    private String dslTemplate;

    @ApiModelProperty("查询模板MD5")
    private String dslTemplateMd5;

    @ApiModelProperty("是否超时，\"true\"/\"false\"")
    private String isTimedOut;

    @ApiModelProperty("查询语句类型")
    private String dslType;

    @ApiModelProperty("查询方式,dsl/sql")
    private String searchType;

    @ApiModelProperty("查询es耗时")
    private Long esCost;

    @ApiModelProperty("查询总耗时")
    private Long totalCost;

    @ApiModelProperty("查询shard个数")
    private Long totalShards;

    @ApiModelProperty("查询总命中数")
    private Long totalHits;

    @ApiModelProperty("查询响应长度")
    private Long responseLen;

    @ApiModelProperty("错误名称")
    private String exceptionName;

    @ApiModelProperty("创建时间")
    private String ariusCreateTime;

    @ApiModelProperty("timeStamp")
    private long timeStamp;

    @ApiModelProperty("indiceSample")
    private String indiceSample;

    @ApiModelProperty("查询字段")
    private Map<String, Long> selectFields;

    @ApiModelProperty("过滤字段")
    private Map<String, Long> whereFields;

    @ApiModelProperty("聚合字段")
    private Map<String, Long> groupByFields;

    @ApiModelProperty("排序字段")
    private Map<String, Long> orderByFields;

    @ApiModelProperty("多type索引查询映射后的索引名称")
    private String destIndexName;

    @ApiModelProperty("请求源ip")
    private String remoteAddr;
}
