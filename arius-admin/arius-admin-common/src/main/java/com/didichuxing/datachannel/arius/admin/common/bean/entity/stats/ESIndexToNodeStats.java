package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.util.CommonUtils.monitorTimestamp2min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESIndexToNodeStats extends BaseESPO {
    /**
     * 时间戳，单位，毫秒
     */
    private long                timestamp;

    /**
     * 索引所属集群
     */
    private String              cluster;

    /**
     * 索引模板名称
     */
    private String              template;

    /**
     * 物理索引模板id
     */
    private long                templateId;

    /**
     * 逻辑模板id
     */
    private long                logicTemplateId;

    /**
     * 索引名称
     */
    private String              index;

    /**
     * 节点ip
     */
    private String              node;

    /**
     * 端口
     */
    private String              port;

    /**
     * rack信息
     */
    private String              rack;

    /**
     * index_node的统计信息
     */
    private Map<String, String> metrics;

    @Override
    public String getKey() {
        return cluster + "@" + node + "@" + index + "@" + monitorTimestamp2min(timestamp);
    }

    @Override
    public String getRoutingValue() {
        return null;
    }
}
