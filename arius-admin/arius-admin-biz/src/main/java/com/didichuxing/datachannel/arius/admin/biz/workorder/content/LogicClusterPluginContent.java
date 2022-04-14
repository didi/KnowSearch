package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LogicClusterPluginContent extends BaseContent {

    /**
     * 逻辑集群id
     */
    private Long   logicClusterId;
    /**
     * 逻辑集群名称
     */
    private String logicClusterName;
    /**
     * 插件地址
     */
    private String pluginPathUrl;
    /**
     * 插件md5
     */
    private String pluginMd5;
}
