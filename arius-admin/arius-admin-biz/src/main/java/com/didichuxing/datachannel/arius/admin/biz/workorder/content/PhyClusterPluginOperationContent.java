package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PhyClusterPluginOperationContent extends BaseContent {

    /**
     * 操作类型 3:安装 4：卸载
     */
    private Integer operationType;
    /**
     * 插件 id
     */
    private Long    pluginId;
    /**
     * 插件文件名称
     */
    private String  pluginFileName;
    /**
     * 插件在文件仓库上的地址
     */
    private String  url;
}
