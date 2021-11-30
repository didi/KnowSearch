package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESResponsePluginInfo {
    /**
     * name 物理节点的名称
     */
    private String name;

    /**
     * component 插件组成名称
     */
    private String component;

    /**
     * version 插件的版本
     */
    private String version;
}
