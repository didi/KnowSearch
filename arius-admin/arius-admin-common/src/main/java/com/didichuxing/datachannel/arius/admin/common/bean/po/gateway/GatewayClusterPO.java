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
    
    private String clusterName;
    private Integer health;
    private Boolean ecmAccess;
    private String memo;
    private Integer componentId;
    private String version;
    private String proxyAddress;
    private String dataCenter;
}