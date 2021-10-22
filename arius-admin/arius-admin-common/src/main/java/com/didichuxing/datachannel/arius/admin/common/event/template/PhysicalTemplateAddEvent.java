package com.didichuxing.datachannel.arius.admin.common.event.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;

/**
 * @author d06679
 * @date 2019/4/18
 */
public class PhysicalTemplateAddEvent extends PhysicalTemplateEvent {

    private IndexTemplatePhy newTemplate;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public PhysicalTemplateAddEvent(Object source, IndexTemplatePhy newTemplate,
                                    IndexTemplateLogicWithPhyTemplates logicWithPhysical) {
        super(source, logicWithPhysical);
        this.newTemplate = newTemplate;
    }

    public IndexTemplatePhy getNewTemplate() {
        return newTemplate;
    }
}
