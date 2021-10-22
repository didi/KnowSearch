package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;

@Data
public class LogicClusterPlugOperationContent extends BaseContent {
    /**
     * 操作类型 3:安装 4：卸载
     */
    private Integer operationType;

    /**
     * 类型 6 插件安装卸载
     */
    private Integer type;
    /**
     * 集群id
     */
    private Long    logicClusterId;

    /**
     * 集群名称
     */
    private String  logicClusterName;

    /**
     * 插件ID
     */
    private String  plugIds;

    /**
     * 插件名称
     */
    private String  plugName;

    /**
     * 插件描述
     */
    private String  plugDesc;

}
