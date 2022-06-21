package com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyAlias;

import java.util.List;

/**
 * 物理模板别名更新事件
 * @author wangshu
 * @date 2020/09/02
 */
public class PhysicalTemplateAliasModifyEvent extends PhysicalMetaDataModifyEvent {

    /**
     * project ID
     */
    private final Integer projectId;

    /**
     * 模板名称
     */
    private final String templateName;

    /**
     * 集群名称
     */
    private final String cluster;

    /**
     * 接受者列表
     */
    private final List<String> receivers;

    /**
     * 更新之前索引模板别名信息
     */
    private final IndexTemplatePhyAlias beforeUpdateAlias;

    /**
     * 更新之后索引模板别名信息
     */
    private final IndexTemplatePhyAlias afterUpdateAlias;

    public PhysicalTemplateAliasModifyEvent(
            Object source,
            Integer projectId,
            String cluster,
            String templateName,
            List<String> receivers,
            IndexTemplatePhyAlias beforeUpdateAlias,
            IndexTemplatePhyAlias afterUpdateAlias) {
        super(source);
        this.projectId = projectId;
        this.templateName = templateName;
        this.cluster = cluster;
        this.receivers = receivers;
        this.beforeUpdateAlias = beforeUpdateAlias;
        this.afterUpdateAlias = afterUpdateAlias;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getCluster() {
        return this.cluster;
    }

    public List<String> getReceivers() {
        return this.receivers;
    }

    public IndexTemplatePhyAlias getBeforeUpdateAlias() {
        return this.beforeUpdateAlias;
    }

    public IndexTemplatePhyAlias getAfterUpdateAlias() {
        return this.afterUpdateAlias;
    }
}