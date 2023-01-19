package com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es;

import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.ConfigChangeComponentContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 集群配置内容
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterPluginConfigContent extends ConfigChangeComponentContent {
  
    private Integer      pluginType;
    private Integer dependComponentId;
    
}