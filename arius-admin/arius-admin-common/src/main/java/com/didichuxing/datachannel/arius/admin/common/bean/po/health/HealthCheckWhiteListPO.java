package com.didichuxing.datachannel.arius.admin.common.bean.po.health;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthCheckWhiteListPO extends BaseESPO {

    /**
     * 模板名称
     */
    private String template;
    /**
     * @see HealthCheckType
     */
    private int    checkType;
    /**
     * 集群名称
     */
    private String cluster;

    @Override
    public String getKey() {
        return checkType + "_" + cluster + "_" + template;
    }

    @Override
    public String getRoutingValue() {
        return null;
    }
}
