package com.didichuxing.datachannel.arius.admin.common.event.template;

import java.util.List;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 *  该类用于集群与目标集群之间的DCDR链路的索引异常，需要重建
 *
 * @author shizeying
 * @date 2022/09/26
 */
@Getter
public class DCDRLinkAbnormalIndicesRebuildEvent extends ApplicationEvent {
    private final String cluster;
    private final String targetCluster;
    private final List<String> indices;
    
   
    
    public DCDRLinkAbnormalIndicesRebuildEvent(Object source, String cluster, String targetCluster,
                                              List<String> indices) {
        super(source);
        this.cluster = cluster;
        this.targetCluster = targetCluster;
        this.indices = indices;
    }
}