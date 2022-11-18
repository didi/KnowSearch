package com.didichuxing.datachannel.arius.admin.common.bean.po.plugin;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 插件信息po
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginInfoPO extends BasePO {
		 /**
    * id 主键自增
    */
    private Long id;

    /**
    *  插件名
    */
    private String name;

    /**
    *  集群 id
    */
    private String clusterId;

    /**
    *  插件版本
    */
    private String version;

    /**
    *  插件存储地址
    */
    private String memo;

    /**
    * 组建 ID
    */
    private Integer componentId;

    /**
    * 插件类型（1. 平台;2. 引擎 )
    */
    private Integer pluginType;

    /**
    * 集群类型 (1.es;2.gateway)
    */
    private Integer clusterType;
}