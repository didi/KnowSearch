package com.didichuxing.datachannel.arius.admin.common.event.resource;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;

/**
 * @author d06679
 * @date 2019/4/25
 */
public class ResourceItemMissEvent extends ApplicationEvent {

    private List<ESClusterLogicRackInfo> items;

    public ResourceItemMissEvent(Object source, List<ESClusterLogicRackInfo> items) {
        super(source);
        this.items = items;
    }

    public List<ESClusterLogicRackInfo> getItems() {
        return items;
    }
}
