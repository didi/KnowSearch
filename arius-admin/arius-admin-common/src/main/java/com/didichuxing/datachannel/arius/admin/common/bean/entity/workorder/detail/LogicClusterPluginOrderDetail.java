package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fengqiongfeng
 * @date 2020/8/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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