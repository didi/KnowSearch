package com.didichuxing.datachannel.arius.admin.common.event.region;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import org.springframework.context.ApplicationEvent;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/7
 * @Comment: region创建事件
 */
public class RegionCreateEvent extends ApplicationEvent {

    private ClusterRegion clusterRegion;

    private Integer share;

    private String operator;

    public RegionCreateEvent(Object source,
                             ClusterRegion clusterRegion,
                             Integer share,
                             String operator) {
        super(source);
        this.clusterRegion = clusterRegion;
        this.operator = operator;
        this.share = share;
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
