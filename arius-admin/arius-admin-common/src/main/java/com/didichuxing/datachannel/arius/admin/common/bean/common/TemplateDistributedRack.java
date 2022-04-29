package com.didichuxing.datachannel.arius.admin.common.bean.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-06-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
