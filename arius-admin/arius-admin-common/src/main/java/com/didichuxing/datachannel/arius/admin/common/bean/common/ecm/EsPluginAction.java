package com.didichuxing.datachannel.arius.admin.common.bean.common.ecm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsPluginAction implements Serializable {

    /**
     * plugin操作类型
     */
    private Integer actionType;

    /**
     * 无效Es集群配置id
     */
    private Long    pluginId;
}
