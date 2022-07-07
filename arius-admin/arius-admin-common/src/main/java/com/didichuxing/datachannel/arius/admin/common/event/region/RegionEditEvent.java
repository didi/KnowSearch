package com.didichuxing.datachannel.arius.admin.common.event.region;

import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author didi
 * @date 2022-05-23 2:36 下午
 */
public class RegionEditEvent extends ApplicationEvent {

    private final List<Long> regionIdList;

    public RegionEditEvent(Object source, List<Long> regionIdList) {
        super(source);
        this.regionIdList = regionIdList;
    }

    public List<Long> getRegionIdList() {
        return regionIdList;
    }
}


