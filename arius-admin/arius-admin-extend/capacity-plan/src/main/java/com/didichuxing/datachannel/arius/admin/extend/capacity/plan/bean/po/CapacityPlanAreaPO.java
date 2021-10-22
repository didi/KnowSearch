package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

import lombok.Data;

/**
 * 容量规划集群PO
 * @author d06679
 * @date 2019-06-24
 */
@Data
public class CapacityPlanAreaPO extends BasePO {

    private Long    id;

    /**
     * 物理集群名称
     */
    private String  clusterName;

    /**
     * 逻辑集群ID
     */
    private Long    resourceId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 配置
     */
    private String  configJson;

    /**
     * 利用率
     */
    private Double  usage;

    /**
     * 超卖率
     */
    private Double  overSold;

}
