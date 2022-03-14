package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.util.CommonUtils.monitorTimestamp2min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESNodeToIndexStats extends BaseESPO {
    /**
     * 单位：毫秒
     */
    private long                timestamp;

    private String              cluster;

    private String              template;

    private long                templateId;

    private long                logicTemplateId;

    private String              index;

    private String              node;

    private String              port;

    private Map<String, String> metrics;

    public void putMetrics(String key, String value) {
        if (null == metrics) {
            metrics = new HashMap<>();
        }

        metrics.put(key, value);
    }

    @Override
    public String getKey() {
        return cluster + "@" + node + "@" + port + "@" + index + "@" + monitorTimestamp2min(timestamp);
    }

    @Override
    public String getRoutingValue() {
        return null;
    }
}
