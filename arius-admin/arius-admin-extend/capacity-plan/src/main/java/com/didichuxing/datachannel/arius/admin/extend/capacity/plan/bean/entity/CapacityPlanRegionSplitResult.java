package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity;

import lombok.Data;

@Data
public class CapacityPlanRegionSplitResult {

    private Long    physicalId;

    private String  templateName;

    private Double  quota;

    private Double  hotDiskQuota;

    private String  tgtRack;

    private boolean exeResult;

}
