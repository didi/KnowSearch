package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESClusterRoleHost {

    /**
     * ip:port 主機名:port = hostname:port
     */
    private String address;

    /**
     * 角色名称
     */
    private String role;

    /**
     * IP、主機名
     */
    private String hostname;

    /**
     * 端口号
     */
    private String port;
}
