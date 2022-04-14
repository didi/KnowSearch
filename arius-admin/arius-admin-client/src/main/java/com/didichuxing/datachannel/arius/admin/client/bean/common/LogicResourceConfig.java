package com.didichuxing.datachannel.arius.admin.client.bean.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源的配置
 * @author wangshu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogicResourceConfig {

    public static final String  QUOTA_CTL_NONE                     = "none";
    public static final String  QUOTA_CTL_DISK                     = "disk";
    public static final String  QUOTA_CTL_ALL                      = "all";

    public static final Integer REPLICA_NUM_DEFAULT                = 1;

    /**
     * quota管控配置
     * 
     * normal集群需要配置为ALL
     * important集群设置为DISK
     * 
     */
    private String              quotaCtl                           = QUOTA_CTL_NONE;

    /**
     * 副本个数 1表示无副本，2表示有一个副本
     * 
     * 目前平台中使用这个值的地方有：
     *  1、容量规划计算模板的factor时
     *  2、容量规划计算模板的CPU消耗时
     * 
     * important和vip集群设置为2
     */
    private Integer             replicaNum                         = REPLICA_NUM_DEFAULT;

    /**
     * 模板创建工单是否自动处理
     *
     * vip集群设置为false
     */
    private Boolean             templateCreateWorkOrderAutoProcess = true;

    /**
     * 模板价值的基准值
     *
     * normal集群设置为0
     * important集群设置为10
     * vip集群设置为20
     */
    private Integer             templateValueBase                  = 0;

    /**
     * 热数据保存天数
     */
    private Integer             hotDataDays                        = -1;

    /**
     * 是否需要治理废弃的模板
     * templateGovern
     */
    private Boolean             templateGovern                     = true;

}
