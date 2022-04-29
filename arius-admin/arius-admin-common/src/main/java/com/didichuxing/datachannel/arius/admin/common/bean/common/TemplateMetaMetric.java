package com.didichuxing.datachannel.arius.admin.common.bean.common;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMetaMetric {

    /***************************************** admin指标 ****************************************************/

    /**
     * 物理模板id
     */
    private Long    physicalId;

    /**
     * 集群
     */
    private String  cluster;

    /**
     * 模板名字
     */
    private String  templateName;

    /**
     * 模板Quota
     */
    private Double  quota;

    /**
     * 数据保存时长
     */
    private Integer expireTime;

    /**
     * shard个数
     */
    private Integer shardNum;

    /**
     * 模板创建时间
     */
    private Date    createTime;

    /**
     * 时间后缀
     */
    private String  dateFormat;

    /**
     * 表达式
     */
    private String  expression;

    /**
     * 副本个数
     */
    private Integer replicaNum;

    /**
     * 热数据的天数
     */
    private Integer hotTime;

    /***************************************** AMS指标 ****************************************************/

    /**
     * 模板总的磁盘消耗  单位G
     */
    private Double  sumIndexSizeG;

    /**
     * 模板最大的索引磁盘消耗  单位G
     */
    private Double  maxIndexSizeG;

    /**
     * 总条数
     */
    private Long    sumDocCount;

    /**
     * 模板最大的索引的文档个数
     */
    private Long    maxIndexDocCount;

    /**
     * tps峰值 单位 条/s
     */
    private Double  maxTps;

    /**
     * 查询的峰值
     */
    private Double  maxQueryTime;

    /**
     * scroll的峰值
     */
    private Double  maxScrollTime;

    /***************************************** 计算指标 ****************************************************/

    /**
     * 实际的磁盘消耗
     */
    private Double  actualDiskG;

    /**
     * 实际的CPU消耗
     */
    private Double  actualCpuCount;

    /**
     * Quota的磁盘消耗
     */
    private Double  quotaDiskG;

    /**
     * Quota的CPU消耗
     */
    private Double  quotaCpuCount;

    /**
     * 综合的磁盘消耗
     */
    private Double  combinedDiskG;

    /**
     * 综合的CPU消耗
     */
    private Double  combinedCpuCount;

}
