package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESClusterRoleHost {

    /**
     * 主机名或IP
     */
    private String hostname;

    /**
     * 角色名称
     */
    private String role;
}
