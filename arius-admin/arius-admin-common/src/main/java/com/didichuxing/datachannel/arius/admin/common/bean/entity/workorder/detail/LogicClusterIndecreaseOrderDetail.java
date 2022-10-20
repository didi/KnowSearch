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
public class LogicClusterIndecreaseOrderDetail extends AbstractOrderDetail {
    /**
     * 集群名称
     */
    private String  logicClusterName;

    /**
     * 逻辑集群的id
     */
    private Long    logicClusterId;

    /**
     * 数据中心
     */
    private String  dataCenter;

    /**
     * dataNode的规格
     */
    private String  dataNodeSpec;

    /**
     * dataNode的个数
     */
    private Integer dataNodeNu;
    
    /**
     * 原有 dataNode 的个数
     */
    private int oldDataNodeNu;
   

    /**
     * 备注
     */
    private String  memo;

    /**
     * 插件上传
     */
    private String  plugins;

    /**
     * 配置文件上传
     */
    private String  config;

  
}