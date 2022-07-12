package com.didiglobal.logi.op.manager.domain.component.event;

import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.event.DomainEvent;

/**
 * @author didi
 * @date 2022-07-12 4:25 下午
 */
public class ComponentEvent extends DomainEvent {
    /**
     * 操作类型
     */
    private int operateType;

    private ComponentEvent(Object source) {
        super(source);
    }

    public static ComponentEvent createInstallEvent(Object source) {
        ComponentEvent event = new ComponentEvent(source);
        event.operateType = OperationEnum.INSTALL.getType();
        event.setDescribe("安装组件");
        return event;
    }

}
