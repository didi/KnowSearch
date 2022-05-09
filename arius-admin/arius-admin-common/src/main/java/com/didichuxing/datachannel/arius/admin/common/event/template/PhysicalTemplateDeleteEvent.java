package com.didichuxing.datachannel.arius.admin.common.event.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfo;

/**
 * @author d06679
 * @date 2019/4/18
 */
public class PhysicalTemplateDeleteEvent extends PhysicalTemplateEvent {

    private IndexTemplatePhyInfo delTemplate;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public PhysicalTemplateDeleteEvent(Object source, IndexTemplatePhyInfo delTemplate,
                                       IndexTemplateInfoWithPhyTemplates logicWithPhysical) {
        super(source, logicWithPhysical);
        this.delTemplate = delTemplate;
    }

    public IndexTemplatePhyInfo getDelTemplate() {
        return delTemplate;
    }
}
