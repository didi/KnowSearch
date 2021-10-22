package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.Data;

/**
 * Created by linyunan on 2021-06-11
 */
@Data
public class LogicClusterDeleteOrderDetail extends AbstractOrderDetail {
    /**
     * 集群名称
     */
    private String  name;

    /**
     * 集群类型
     */
    private Integer type;

    /**
     * 服务等级
     */
    private Integer level;

    /**
     * 数据中心
     */
    private String  dataCenter;
}
