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
public class LogicClusterPlugOperationOrderDetail extends AbstractOrderDetail {
    /**
     * 操作类型 3:安装 4：卸载
     */
    private String operationType;
    /**
     * 集群id
     */
    private Long   logicClusterId;

    /**
     * 集群名称
     */
    private String logicClusterName;

    /**
     * 插件ID
     */
    private Long   plugId;

    /**
     * 插件名称
     */
    private String plugName;

    /**
     * 插件描述
     */
    private String plugDesc;
}