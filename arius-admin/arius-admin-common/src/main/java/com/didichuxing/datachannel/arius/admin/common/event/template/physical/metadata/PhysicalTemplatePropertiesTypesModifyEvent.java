package com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata;

import java.util.List;

/**
 * 物理模板type变更事件
 * @author wangshu
 * @date 2020/09/02
 */
public class PhysicalTemplatePropertiesTypesModifyEvent extends PhysicalMetaDataModifyEvent {

    /**
     * APP ID
     */
    private final Integer appId;

    /**
     * 集群
     */
    private final String cluster;

    /**
     * 模板名称
     */
    private final String templateName;

    /**
     * 接受者列表
     */
    private final List<String> receivers;

    /**
     * 更新前物理模板properties types
     */
    private final List<String> beforeUpdateTypes;

    /**
     * 更新后物理模板properties types
     */
    private final List<String> afterUpdateTypes;

    public PhysicalTemplatePropertiesTypesModifyEvent(
            Object source,
            Integer appId,
            String cluster,
            String templateName,
            List<String> receivers,
            List<String> beforeUpdateTypes,
            List<String> afterUpdateTypes) {
        super(source);
        this.appId = appId;
        this.cluster = cluster;
        this.templateName = templateName;
        this.receivers = receivers;
        this.beforeUpdateTypes = beforeUpdateTypes;
        this.afterUpdateTypes = afterUpdateTypes;
    }

    public Integer getAppId() {
        return this.appId;
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

    public List<String> getBeforeUpdateTypes() {
        return this.beforeUpdateTypes;
    }

    public List<String> getAfterUpdateTypes() {
        return this.afterUpdateTypes;
    }
}
