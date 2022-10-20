package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhyClusterPluginOperationOrderDetail extends AbstractOrderDetail {
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
     * 插件在url上的地址
     */
    private String  url;
}
