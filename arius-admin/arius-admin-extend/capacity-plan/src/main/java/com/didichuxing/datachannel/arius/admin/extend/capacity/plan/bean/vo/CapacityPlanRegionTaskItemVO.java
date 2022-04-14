package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.vo;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
@ApiModel(description = "任务明细信息 ")
public class CapacityPlanRegionTaskItemVO {

    @ApiModelProperty("id")
    private Long    id;

    @ApiModelProperty("任务ID")
    private Long    taskId;

    /***************************************** admin指标 ****************************************************/

    /**
     * 物理模板id
     * -
     */
    @ApiModelProperty("索引ID")
    private Long    physicalId;

    /**
     * 集群
     */
    @ApiModelProperty("物理集群")
    private String  cluster;

    /**
     * 模板名字
     * -
     */
    @ApiModelProperty("模板")
    private String  templateName;

    /**
     * Quota
     * -
     */
    @ApiModelProperty("配额")
    private Double  quota;

    /**
     * 数据保存时长
     */
    @ApiModelProperty("保存天数")
    private Integer expireTime;

    /**
     * shard个数
     */
    @ApiModelProperty("shard个数")
    private Integer shardNum;

    /**
     * 模板创建时间
     */
    @ApiModelProperty("创建时间")
    private Date    createTime;

    /**
     * 时间后缀
     */
    @ApiModelProperty("分区周期")
    private String  dateFormat;

    /**
     * 表达式
     */
    @ApiModelProperty("表达式")
    private String  expression;

    /***************************************** AMS指标 ****************************************************/

    /**
     * 模板总的磁盘消耗  单位G
     * -
     */
    @ApiModelProperty("索引总大小(G)")
    private Double  sumIndexSizeG;

    /**
     * 总条数
     */
    @ApiModelProperty("文档总条数")
    private Long    sumDocCount;

    /**
     * 冷数据的天数
     */
    @ApiModelProperty("热数据的天数")
    private Integer hotDay;

    /**
     * tps峰值 单位 W/s
     * -
     */
    @ApiModelProperty("tps峰值(w/s)")
    private Double  maxTps;

    /**
     * 查询的峰值
     */
    @ApiModelProperty("查询时间(ms)")
    private Double  maxQueryTime;

    /**
     * scroll的峰值
     */
    @ApiModelProperty("滚动查询时间(ms)")
    private Double  maxScrollTime;

    /**
     * 副本个数
     */
    @ApiModelProperty("副本个数")
    private Integer replicaNum;

    /***************************************** 计算指标 ****************************************************/

    /**
     * 实际的磁盘消耗
     * -
     */
    @ApiModelProperty("磁盘实际消耗")
    private Double  actualDiskG;

    /**
     * 实际的CPU消耗
     * -
     */
    @ApiModelProperty("CPU实际消耗")
    private Double  actualCpuCount;

    /**
     * Quota的磁盘消耗
     */
    @ApiModelProperty("磁盘配额")
    private Double  quotaDiskG;

    /**
     * Quota的CPU消耗
     */
    @ApiModelProperty("CPU配额")
    private Double  quotaCpuCount;

    /**
     * 综合的磁盘消耗
     */
    @ApiModelProperty("综合磁盘消耗")
    private Double  combinedDiskG;

    /**
     * 综合的CPU消耗
     */
    @ApiModelProperty("综合CPU消耗")
    private Double  combinedCpuCount;

}
