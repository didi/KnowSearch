package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySettings;
import com.didichuxing.datachannel.arius.admin.common.constant.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.util.MapUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * @author liuchengxiang
 * @date 2022/2/17
 */
@Data
@NoArgsConstructor
public class TemplateSettingOperateRecord extends TemplateOperateRecord {

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

    public TemplateSettingOperateRecord(IndexTemplatePhySettings oldConfig, IndexTemplatePhySettings newConfig ) {
        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
        this.diffContext = MapUtils.findChangedWithDestV(oldConfig.flatSettings(), newConfig.flatSettings());
        this.operateType = TemplateOperateRecordEnum.SETTING.getCode();
    }

}
