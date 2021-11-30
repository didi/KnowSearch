package com.didichuxing.datachannel.arius.admin.common.event.resource;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;

/**
 * @author d06679
 * @date 2019/4/25
 */
public class ResourceItemMissEvent extends ApplicationEvent {

    private List<ClusterLogicRackInfo> items;

    public ResourceItemMissEvent(Object source, List<ClusterLogicRackInfo> items) {
        super(source);
        this.items = items;
    }

    public List<ClusterLogicRackInfo> getItems() {
        return items;
    }
}
