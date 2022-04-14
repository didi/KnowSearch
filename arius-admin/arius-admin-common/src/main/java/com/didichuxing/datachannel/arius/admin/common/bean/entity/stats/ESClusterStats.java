package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import static com.didichuxing.datachannel.arius.admin.common.util.CommonUtils.monitorTimestamp2min;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.constant.PercentilesEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESClusterStats extends BaseESPO {
    /**
     * 集群名称，all则表示全集群信息
     */
    private String              cluster;

    /**
     * 统计的时间戳，单位：毫秒
     */
    private long                timestamp;

    /**
     * 是否是物理集群 1：是；0：不是
     */
    private long                physicCluster;

    /**
     * 分位类型
     * @see PercentilesEnum
     */
    private String              percentilesType;

    /**
     * 统计信息
     */
    private ESClusterStatsCells statis;

    /**
     * 数据中心
     */
    private String              dataCenter;

    @Override
    public String getKey() {
        return String.format("%s@%s@%s@%d", dataCenter, cluster, percentilesType, monitorTimestamp2min(timestamp));
    }

    @Override
    public String getRoutingValue() {
        return cluster;
    }

}
