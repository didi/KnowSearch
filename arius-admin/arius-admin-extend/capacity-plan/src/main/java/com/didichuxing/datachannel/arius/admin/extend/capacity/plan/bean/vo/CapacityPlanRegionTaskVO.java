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
@ApiModel(description = "规划任务信息")
public class CapacityPlanRegionTaskVO {

    /**
     * 主键
     */
    @ApiModelProperty("任务ID")
    private Long    id;

    /**
     * regionId
     */
    @ApiModelProperty("regionID")
    private Long    regionId;

    /**
     * 0 初始化region  1 plan    2 check
     * -
     */
    @ApiModelProperty("任务名称（1 plan；2 check）")
    private Integer task;

    /**
     * 类型 0 初始化  1 扩容  2 缩容
     * -
     */
    @ApiModelProperty("任务类型（0 初始化；1 扩容；2 缩容）")
    private Integer type;

    /**
     * 状态  1 进行中   2 完成
     * -
     */
    @ApiModelProperty("任务状态（ 1 进行中；2 完成）")
    private Integer status;

    /**
     * 源rack
     * -
     */
    @ApiModelProperty("源rack")
    private String  srcRacks;

    /**
     * 变化rack
     * -
     */
    @ApiModelProperty("变化rack")
    private String  deltaRacks;

    /**
     * 磁盘消耗(G)
     * -
     */
    @ApiModelProperty("region磁盘消耗（G）")
    private Double  regionCostDiskG;

    /**
     * CPU消耗(核)
     * -
     */
    @ApiModelProperty("regionCPU消耗")
    private Double  regionCostCpuCount;

    /**
     * 开始时间
     */
    @ApiModelProperty("任务开始时间")
    private Date    startTime;

    /**
     * 晚上时间
     */
    @ApiModelProperty("任务结束时间")
    private Date    finishTime;

}
