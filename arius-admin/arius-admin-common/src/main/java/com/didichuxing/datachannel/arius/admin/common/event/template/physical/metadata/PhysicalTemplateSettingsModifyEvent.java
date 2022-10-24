package com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySetting;

import java.util.List;

/**
 * 物理模板settings更新事件
 * @author wangshu
 * @date 2020/09/02
 */
public class PhysicalTemplateSettingsModifyEvent extends PhysicalMetaDataModifyEvent {
    private final Integer                 projectId;
    private final String                  cluster;
    private final String                  templateName;
    private final List<String>            receivers;
    private final IndexTemplatePhySetting beforeUpdateSettings;
    private final IndexTemplatePhySetting afterUpdateSettings;

    public PhysicalTemplateSettingsModifyEvent(Object source, Integer projectId, String cluster, String templateName,
                                               List<String> receivers, IndexTemplatePhySetting beforeUpdateSettings,
                                               IndexTemplatePhySetting afterUpdateSettings) {
        super(source);
        this.projectId = projectId;
        this.cluster = cluster;
        this.templateName = templateName;
        this.receivers = receivers;
        this.beforeUpdateSettings = beforeUpdateSettings;
        this.afterUpdateSettings = afterUpdateSettings;
    }

    public Integer getProjectId() {
        return this.projectId;
    }

    public String getCluster() {
        return this.cluster;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public List<String> getReceivers() {
        return this.receivers;
    }

    public IndexTemplatePhySetting getBeforeUpdateSettings() {
        return this.beforeUpdateSettings;
    }

    public IndexTemplatePhySetting getAfterUpdateSettings() {
        return this.afterUpdateSettings;
    }
}