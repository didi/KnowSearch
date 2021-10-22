package com.didichuxing.datachannel.arius.admin.client.bean.common;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-25
 */
@Data
public class TemplateDistributedRack {

    /**
     * 物理集群
     */
    private String cluster;

    /**
     * rack
     */
    private String rack;

    /**
     * 当前资源是否满足
     */
    private Boolean isResourceSuitable;

    /**
     * 资源是否充足
     * @return
     */
    public boolean isResourceMatched() {
        return (isResourceSuitable == null || isResourceSuitable);
    }
}
