package com.didichuxing.datachannel.arius.admin.common.event.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfo;

/**
 * @author d06679
 * @date 2019/4/18
 */
public class PhysicalTemplateModifyEvent extends PhysicalTemplateEvent {

    private IndexTemplatePhyInfo oldTemplate;

    private IndexTemplatePhyInfo newTemplate;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public PhysicalTemplateModifyEvent(Object source, IndexTemplatePhyInfo oldTemplate,
                                       IndexTemplatePhyInfo newTemplate,
                                       IndexTemplateInfoWithPhyTemplates logicWithPhysical) {
        super(source, logicWithPhysical);
        this.oldTemplate = oldTemplate;
        this.newTemplate = newTemplate;
    }

    public IndexTemplatePhyInfo getOldTemplate() {
        return oldTemplate;
    }

    public IndexTemplatePhyInfo getNewTemplate() {
        return newTemplate;
    }
}
