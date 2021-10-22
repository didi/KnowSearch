package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po;

import java.util.Date;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
public class CapacityPlanRegionTaskItemPO {
    private Long    id;

    private Long    taskId;

    private Long    physicalId;

    private String  cluster;

    private String  templateName;

    private Double  quota;

    private Integer expireTime;

    private Integer shardNum;

    private Date    createTime;

    private String  dateFormat;

    private String  expression;

    /***************** 以下的这些值不是admin管理的，在region初始化任务中不会获取，保存数据库时需要手动赋值 *********************/

    private Double  sumIndexSizeG;

    private Long    sumDocCount;

    private Integer hotDay;

    private Double  maxTps;

    private Double  maxQueryTime;

    private Double  maxScrollTime;

    private Integer replicaNum;

    private Double  actualDiskG;

    private Double  actualCpuCount;

    private Double  quotaDiskG;

    private Double  quotaCpuCount;

    private Double  combinedDiskG;

    private Double  combinedCpuCount;
}
