package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.didichuxing.datachannel.arius.admin.common.util.CommonUtils.monitorTimestamp2min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESClusterStats extends BaseESPO {
    /**
     * 集群名称，all则表示全集群信息
     */
    private String cluster;

    /**
     * 统计的时间戳，单位：毫秒
     */
    private long  timestamp;

    /**
     * 是否是物理集群 1：是；0：不是
     */
    private long  physicCluster;

    /**
     * 统计值
     */
    private ESClusterTempBean statis;

    /**
     * 数据中心
     */
    private String dataCenter;

    @Override
    public String getKey() {
        return String.format("%s@%s@%d", dataCenter, cluster, monitorTimestamp2min(timestamp));
    }

}
