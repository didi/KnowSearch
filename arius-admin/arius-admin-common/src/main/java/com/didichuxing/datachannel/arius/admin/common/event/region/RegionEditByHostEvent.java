package com.didichuxing.datachannel.arius.admin.common.event.region;

import java.util.List;

/**
 * @Authoer: zyl
 * @Date: 2022/10/24
 * @Version: 1.0
 */
public class RegionEditByHostEvent extends RegionEditEvent{

    public RegionEditByHostEvent(Object source, List<Long> regionIdList) {
        super(source, regionIdList);
    }
}
