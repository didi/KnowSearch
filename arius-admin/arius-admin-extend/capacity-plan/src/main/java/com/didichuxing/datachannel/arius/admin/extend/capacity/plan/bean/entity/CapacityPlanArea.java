package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanConfig;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
public class CapacityPlanArea {

    /**
     * 主键
     */
    private Long               id;

    /**
     * 集群名字
     */
    private String             clusterName;

    /**
     * 逻辑资源的id
     */
    private Long               resourceId;

    /**
     * 状态  1 规划中   2 暂停规划
     */
    private Integer            status;

    /**
     * 配置
     */
    private String             configJson;

    /**
     * 结构化的配置
     */
    private CapacityPlanConfig config;

    /**
     * 利用率
     */
    private Double             usage;

    /**
     * 超卖率
     */
    private Double             overSold;

}
