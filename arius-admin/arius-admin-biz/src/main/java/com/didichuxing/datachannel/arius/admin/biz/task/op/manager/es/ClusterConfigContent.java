package com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es;

import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.ConfigChangeComponentContent;
import lombok.Getter;
import lombok.Setter;

/**
 * 集群配置内容
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */
@Getter
@Setter
public class ClusterConfigContent extends ConfigChangeComponentContent {
    public ClusterConfigContent(Integer componentId) {
        super(componentId);
    }
    
    /**
     * 原因
     */
    private String reason;
}