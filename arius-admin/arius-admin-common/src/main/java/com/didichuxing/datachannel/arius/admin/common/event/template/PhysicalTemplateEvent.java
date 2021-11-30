package com.didichuxing.datachannel.arius.admin.common.event.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;

/**
 * @author d06679
 * @date 2019/4/18
 */
public abstract class PhysicalTemplateEvent extends TemplateEvent {

    private IndexTemplateLogicWithPhyTemplates logicWithPhysical;

    protected PhysicalTemplateEvent(Object source, IndexTemplateLogicWithPhyTemplates logicWithPhysical) {
        super(source);
        this.logicWithPhysical = logicWithPhysical;
    }

    public IndexTemplateLogicWithPhyTemplates getLogicWithPhysical() {
        return logicWithPhysical;
    }
}
