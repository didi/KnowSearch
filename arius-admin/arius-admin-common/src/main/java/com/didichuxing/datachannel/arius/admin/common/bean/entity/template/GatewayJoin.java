package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

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
public class GatewayJoin {

    /**
     * 请求path
     */
    private String uri;
    /**
     * 请求id
     */
    private String  requestId;
    /**
     * projectid
     */
    private Integer projectId;
    /**
     * 索引名称
     */
    private String  indices;
    /**
     * type名称
     */
    private String typeName;
    /**
     * 查询命中索引信息json
     */
    private String index;
    /**
     * 查询语句
     */
    private String dsl;
    /**
     * 查询模板
     */
    private String dslTemplate;
    /**
     * 查询模板MD5
     */
    private String dslTemplateMd5;
    /**
     * 是否超时，"true"/"false"
     */
    private String isTimedOut;
    /**
     * 查询语句类型
     */
    private String dslType;
    /**
     * 查询方式,dsl/sql
     */
    private String searchType;
    /**
     * 查询es耗时
     */
    private Long esCost;
    /**
     * 查询总耗时
     */
    private Long totalCost;
    /**
     * 查询shard个数
     */
    private Long totalShards;
    /**
     * 查询总命中数
     */
    private Long totalHits;
    /**
     * 查询响应长度
     */
    private Long responseLen;
    /**
     * 错误名称
     */
    private String exceptionName;
    /**
     * 创建时间
     */
    private String ariusCreateTime;
    /**
     * timeStamp
     */
    private long timeStamp;
    /**
     * indiceSample
     */
    private String indiceSample;
    /**
     * 查询字段
     */
    private Map<String, Long> selectFields;
    /**
     * 过滤字段
     */
    private Map<String, Long> whereFields;
    /**
     * 聚合字段
     */
    private Map<String, Long> groupByFields;
    /**
     *排序字段
     */
    private Map<String, Long> orderByFields;
    /**
     * 多type索引查询映射后的索引名称
     */
    private String destIndexName;
    /**
     * 请求源ip
     */
    private String remoteAddr;
}