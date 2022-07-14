package com.didichuxing.datachannel.arius.admin.common.event.region;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import org.springframework.context.ApplicationEvent;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/7
 * @Comment: region绑定事件
 */
public class RegionBindEvent extends ApplicationEvent {

    private ClusterRegion clusterRegion;

    private Integer       share;

    private String        operator;

    public RegionBindEvent(Object source, ClusterRegion clusterRegion, Integer share, String operator) {
        super(source);
        this.clusterRegion = clusterRegion;
        this.share = share;
        this.operator = operator;
    }

    public ClusterRegion getClusterRegion() {
        return clusterRegion;
    }

    public Integer getShare() {
        return share;
    }

    public String getOperator() {
        return operator;
    }
}
