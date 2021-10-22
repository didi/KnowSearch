package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class EsPluginAction {

    /**
     * plugin操作类型
     */
    private Integer actionType;

    /**
     * 无效Es集群配置id
     */
    private Long    pluginId;
}
