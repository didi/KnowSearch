package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import static com.didichuxing.datachannel.arius.admin.common.util.CommonUtils.monitorTimestamp2min;

import java.util.HashMap;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "集群索引中的统计信息")
public class ESIndexStats extends BaseESPO {
    /**
     * 统计的时间戳，单位：毫秒
     */
    private long                timestamp;

    /**
     * 统计的集群名称
     */
    private String              cluster;

    /**
     * 统计的集群中的索引模板名称
     */
    private String              template;

    /**
     * 统计的集群中的索引模板的id
     */
    private long                templateId;

    /**
     * 统计的集群中的索引模板的逻辑id
     */
    private long                logicTemplateId;

    /**
     * 统计的集群中的索引的名称
     */
    private String              index;

    /**
     * 统计的集群中的索引的shard个数
     */
    private long                shardNu;

    /**
     * 统计的集群中的索引的具体指标
     */
    private Map<String, String> metrics;

    public void putMetrics(String key, String value) {
        if (null == metrics) {
            metrics = new HashMap<>();
        }

        metrics.put(key, value);
    }

    @Override
    public String getKey() {
        return cluster + "@" + index + "@" + monitorTimestamp2min(timestamp);
    }

    @Override
    public String getRoutingValue() {
        return index;
    }
}
