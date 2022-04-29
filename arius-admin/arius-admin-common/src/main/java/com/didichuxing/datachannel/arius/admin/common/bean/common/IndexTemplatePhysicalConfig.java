package com.didichuxing.datachannel.arius.admin.common.bean.common;

import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexTemplatePhysicalConfig {

    /**
     * 人工设置的pipeline限流值
     */
    private Integer                                         manualPipeLineRateLimit;

    /**
     * 动态pipeline限流值，pipeLineRateLimit 必须小于 manualPipeLineRateLimit
     */
    private Integer                                         pipeLineRateLimit;

    /**
     * kafkatopic
     */
    private String                                          kafkaTopic;

    /**
     * 可以访问的APP
     */
    private Set<Integer>                                    accessApps;

    /**
     * frozen配置
     */
    private Boolean                                         frozen;

    /**
     * 用于索引多type改造   是否启用索引名称映射 0 禁用 1 启用
     */
    private Boolean                                         mappingIndexNameEnable;

    /**
     * 是否是默认写索引标识
     */
    private Boolean                                         defaultWriterFlags;

    /**
     * 组ID
     */
    private String                                          groupId;

    /**
     * 多type索引type名称到单type索引模板名称的映射
     */
    private Map<String/*typeName*/, String/*templateName*/> typeIndexMapping;
}
