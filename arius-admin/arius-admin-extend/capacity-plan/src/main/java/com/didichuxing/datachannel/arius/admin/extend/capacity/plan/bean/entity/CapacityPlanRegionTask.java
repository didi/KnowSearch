package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity;

import java.util.Date;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
public class CapacityPlanRegionTask {

    /**
     * 主键
     */
    private Long    id;

    /**
     * regionId
     */
    private Long    regionId;

    /**
     * 0 初始化region  1 plan    2 check
     * -
     */
    private Integer task;

    /**
     * 类型 0 初始化  1 扩容  2 缩容
     * -
     */
    private Integer type;

    /**
     * 状态  1 进行中   2 完成
     * -
     */
    private Integer status;

    /**
     * 源rack
     * -
     */
    private String  srcRacks;

    /**
     * 变化rack
     * -
     */
    private String  deltaRacks;

    /**
     * 磁盘消耗(G)
     * -
     */
    private Double  regionCostDiskG;

    /**
     * CPU消耗(核)
     * -
     */
    private Double  regionCostCpuCount;

    /**
     * 开始时间
     */
    private Date    startTime;

    /**
     * 完成时间
     */
    private Date    finishTime;

}
