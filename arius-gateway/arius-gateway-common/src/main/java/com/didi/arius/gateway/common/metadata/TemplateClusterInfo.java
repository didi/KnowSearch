
package com.didi.arius.gateway.common.metadata;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class TemplateClusterInfo {

    /**
     * 允许访问的appid列表
     */
    private Set<Integer> accessApps;

    /**
     * 索引模板对应的集群名称
     */
    private String cluster;

    /**
     * 索引模板对应的topic名称
     */
    private String topic;

    /**
     * 用于索引多type改造   是否启用索引名称映射 0 禁用 1 启用
     */
    private Boolean mappingIndexNameEnable;

    /**
     * 多type索引type名称到单type索引模板名称的映射
     */
    private Map<String/*typeName*/,String/*templateName*/> typeIndexMapping;

}
