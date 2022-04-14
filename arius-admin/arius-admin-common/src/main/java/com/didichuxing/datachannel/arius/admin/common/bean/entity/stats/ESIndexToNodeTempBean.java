package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESIndexToNodeTempBean {
    /**
     * 单位：毫秒
     */
    private long        timestamp;

    /**
     * 集群名称
     */
    private String      cluster;

    /**
     * 模板名称
     */
    private String      template;

    /**
     * 物理模板id
     */
    private long        templateId;

    /**
     * 逻辑模板id
     */
    private long        logicTemplateId;

    /**
     * 索引名称
     */
    private String      index;

    /**
     * 索引所在节点
     */
    private Set<String> nodes;
}
