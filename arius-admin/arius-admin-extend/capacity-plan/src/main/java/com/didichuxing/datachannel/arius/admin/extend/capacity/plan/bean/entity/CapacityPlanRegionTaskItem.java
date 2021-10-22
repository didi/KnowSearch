package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity;

import java.util.Date;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
public class CapacityPlanRegionTaskItem {

    private Long    id;

    private Long    taskId;

    /***************************************** admin指标 ****************************************************/

    /**
     * 物理模板id
     * -
     */
    private Long    physicalId;

    /**
     * 集群
     */
    private String  cluster;

    /**
     * 模板名字
     * -
     */
    private String  templateName;

    /**
     * Quota
     * -
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

    /***************************************** AMS指标 ****************************************************/

    /**
     * 模板总的磁盘消耗  单位G
     * -
     */
    private Double  sumIndexSizeG;

    /**
     * 总条数
     */
    private Long    sumDocCount;

    /**
     * 冷数据的天数
     */
    private Integer hotDay;

    /**
     * tps峰值 单位 W/s
     * -
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

    /**
     * 副本个数
     */
    private Integer replicaNum;

    /***************************************** 计算指标 ****************************************************/

    /**
     * 实际的磁盘消耗
     * -
     */
    private Double  actualDiskG;

    /**
     * 实际的CPU消耗
     * -
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
