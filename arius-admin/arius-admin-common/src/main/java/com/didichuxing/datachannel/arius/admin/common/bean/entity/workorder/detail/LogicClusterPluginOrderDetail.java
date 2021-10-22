package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.Data;

/**
 * @author fengqiongfeng
 * @date 2020/8/24
 */
@Data
public class LogicClusterPluginOrderDetail extends AbstractOrderDetail {
    /**
     * 逻辑集群id
     */
    private Long   logicClusterId;
    /**
     * 逻辑集群名称
     */
    private String logicClusterName;
}