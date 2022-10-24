package com.didichuxing.datachannel.arius.admin.common.bean.entity;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * 多type索引信息
 */
@Data
public class MulityTypeTemplatesInfo {

    /**
     * 多type索引映射,目标索引到源索引映射
     */
    private Map<String/*destTemplateName*/, String/*sourceTemplateName*/>      dest2SourceTemplateMap = Maps
        .newHashMap();

    /**
     * 多type索引映射,源索引映射到目标索引
     */
    private Map<String/*sourceTemplateName*/, Set<String/*destTemplateName*/>> source2DestTemplateMap = Maps
        .newHashMap();

}
