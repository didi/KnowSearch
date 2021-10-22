package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;

@Data
public class LogicClusterIndecreaseContent extends BaseContent {
    /**
     * 逻辑集群名称
     */
    private String logicClusterName;

    /**
     * 逻辑集群的id
     */
    private Long   logicClusterId;

    /**
     * dataNode的规格
     */
    private String dataNodeSpec;

    /**
     * dataNode的个数
     */
    private int    dataNodeNu;
    /**
     * 备注
     */
    private String memo;
}
