package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
public class CapacityPlanRegionTaskPO extends BasePO {

    private Long    id;

    private Long    regionId;

    private Integer task;

    private Integer type;

    private Integer status;

    private String  srcRacks;

    private String  deltaRacks;

    private Double  regionCostDiskG;

    private Double  regionCostCpuCount;

    private Date    startTime;

    private Date    finishTime;

}
