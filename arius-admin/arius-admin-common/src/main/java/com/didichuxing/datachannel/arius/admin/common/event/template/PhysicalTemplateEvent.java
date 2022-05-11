package com.didichuxing.datachannel.arius.admin.common.event.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;

/**
 * @author d06679
 * @date 2019/4/18
 */
public abstract class PhysicalTemplateEvent extends TemplateEvent {

    private IndexTemplateWithPhyTemplates logicWithPhysical;

    protected PhysicalTemplateEvent(Object source, IndexTemplateWithPhyTemplates logicWithPhysical) {
        super(source);
        this.logicWithPhysical = logicWithPhysical;
    }

    public IndexTemplateWithPhyTemplates getLogicWithPhysical() {
        return logicWithPhysical;
    }
}
