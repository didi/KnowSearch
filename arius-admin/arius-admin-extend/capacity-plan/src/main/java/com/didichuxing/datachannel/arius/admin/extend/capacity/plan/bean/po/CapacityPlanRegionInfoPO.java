package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.Data;

/**
 * 容量规划Region容量信息，对应es_cluster_region_capacity_info表
 *
 * @author wangshu
 * @date 2020/09/15
 */
@Data
public class CapacityPlanRegionInfoPO extends BasePO {

    /**
     * 主键id
     */
    private Long id;

    /**
     * region ID（关联es_cluster_region表的ID）
     */
    private Long regionId;

    /**
     * 空闲Quota
     */
    private Double freeQuota;

    /**
     * 配置
     */
    private String configJson;

    /**
     * 是否接单
     */
    private Integer share;

    /**
     * 利用率
     */
    private Double usage;

    /**
     * 超卖率
     */
    private Double overSold;

}
