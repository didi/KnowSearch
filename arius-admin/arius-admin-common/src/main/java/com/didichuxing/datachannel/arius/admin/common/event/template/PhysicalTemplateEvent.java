package com.didichuxing.datachannel.arius.admin.common.event.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithPhyTemplates;

/**
 * @author d06679
 * @date 2019/4/18
 */
public abstract class PhysicalTemplateEvent extends TemplateEvent {

    private IndexTemplateInfoWithPhyTemplates logicWithPhysical;

    protected PhysicalTemplateEvent(Object source, IndexTemplateInfoWithPhyTemplates logicWithPhysical) {
        super(source);
        this.logicWithPhysical = logicWithPhysical;
    }

    public IndexTemplateInfoWithPhyTemplates getLogicWithPhysical() {
        return logicWithPhysical;
    }
}
