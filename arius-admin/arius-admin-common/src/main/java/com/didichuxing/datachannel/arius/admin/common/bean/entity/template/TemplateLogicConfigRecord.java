package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * @author liuchengxiang
 * @date 2022/2/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateLogicConfigRecord {

    /**
     * 旧的setting
     */
    private IndexTemplatePhySettings oldConfig;

    /**
     * 新的setting
     */
    private IndexTemplatePhySettings newConfig;

    /**
     * 新旧setting 不同的位点
     */
    private Map<String, String> diffContext;

}
