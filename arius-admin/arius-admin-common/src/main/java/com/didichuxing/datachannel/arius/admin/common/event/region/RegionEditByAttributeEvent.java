package com.didichuxing.datachannel.arius.admin.common.event.region;

import java.util.List;

/**
 * @Authoer: zyl
 * @Date: 2022/10/24
 * @Version: 1.0
 */
public class RegionEditByAttributeEvent extends RegionEditEvent{

    private String attributeKey;

    public RegionEditByAttributeEvent(Object source, List<Long> regionIdList, String attributeKey) {
        super(source, regionIdList);
        this.attributeKey = attributeKey;
    }

    public String getAttributeKey() {
        return attributeKey;
    }
}
