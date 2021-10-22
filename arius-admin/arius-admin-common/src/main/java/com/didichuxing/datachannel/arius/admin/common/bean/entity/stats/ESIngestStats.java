package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.util.CommonUtils.monitorTimestamp2min;

/**
 * author weizijun
 * date：2019-11-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESIngestStats extends BaseESPO {
    /**
     * 单位：毫秒
     */
    private long                timestamp;

    /**
     * 集群名称
     */
    private String              cluster;

    /**
     * 模板名称
     */
    private String              template;

    /**
     * 模板id
     */
    private long                templateId;

    /**
     * 逻辑模板id
     */
    private long                logicTemplateId;

    /**
     * 节点
     */
    private String              node;

    /**
     * 端口
     */
    private String              port;

    /**
     * 指标
     */
    private Map<String, String> metrics;

    public void putMetrics(String key, String value){
        if(null == metrics){metrics = new HashMap<>();}

        metrics.put(key, value);
    }

    @Override
    public String getKey() {
        return cluster + "@" + node + "@" + template + "@" + monitorTimestamp2min(timestamp);
    }
}
