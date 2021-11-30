package com.didichuxing.datachannel.arius.admin.common.bean.po.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayClusterPO extends BasePO {

    private Integer id;

    private String  clusterName;

}
