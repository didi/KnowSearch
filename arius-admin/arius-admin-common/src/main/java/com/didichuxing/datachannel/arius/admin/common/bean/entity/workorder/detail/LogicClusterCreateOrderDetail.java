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
public class LogicClusterCreateOrderDetail extends AbstractOrderDetail {
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

    /**
     * dataNode的规格
     */
    private String  dataNodeSpec;

    /**
     * dataNode的个数
     */
    private Integer dataNodeNu;

 

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