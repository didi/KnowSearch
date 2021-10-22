package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto;

import java.util.Date;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegionTaskItem;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
public class CapacityPlanRegionTaskDTO {

    /**
     * 主键
     */
    private Long                             id;

    /**
     * regionId
     */
    private Long                             regionId;

    /**
     * 0 初始化region  1 plan    2 check
     */
    private Integer                          task;

    /**
     * 类型  1 扩容  2 缩容
     */
    private Integer                          type;

    /**
     * 状态  1 进行中   2 完成
     */
    private Integer                          status;

    /**
     * 源rack
     */
    private String                           srcRacks;

    /**
     * 变化的rack
     */
    private String                           deltaRacks;

    /**
     * 磁盘消耗
     */
    private Double                           regionCostDiskG;

    /**
     * CPU消耗
     */
    private Double                           regionCostCpuCount;

    /**
     * 开始时间
     */
    private Date                             startTime;

    /**
     * 晚上时间
     */
    private Date                             finishTime;

    /**
     * 任务详情
     */
    private List<CapacityPlanRegionTaskItem> taskItems;

}
