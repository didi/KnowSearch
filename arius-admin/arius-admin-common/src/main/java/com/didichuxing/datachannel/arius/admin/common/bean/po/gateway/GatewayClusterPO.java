package com.didichuxing.datachannel.arius.admin.common.bean.po.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关集群po
 *
 * @author shizeying
 * @date 2022/10/31
 * @since 0.3.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayClusterPO extends BasePO {

    private Integer id;
    
    /**
     * 集群名称
     */
    private String clusterName;
    
    /**
     * 健康
     */
    private Integer health;
    
    /**
     * 接入ecm
     */
    private Boolean ecmAccess;
    
    /**
     * 备忘录
     */
    private String memo;
    
    /**
     * 组件id
     */
    private Integer componentId;
    
    /**
     * 版本
     */
    private String version;
    
    /**
     * 代理地址
     */
    private String proxyAddress;
    
    /**
     * 数据中心
     */
    private String dataCenter;
}