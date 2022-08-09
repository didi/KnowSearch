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

    public int getOperateType() {
        return operateType;
    }

    public void setOperateType(int operateType) {
        this.operateType = operateType;
    }

    private ComponentEvent(Object source) {
        super(source);
    }

    public static ComponentEvent createInstallEvent(Object source) {
        ComponentEvent event = new ComponentEvent(source);
        event.operateType = OperationEnum.INSTALL.getType();
        event.setDescribe(OperationEnum.INSTALL.getDescribe());
        return event;
    }

    public static ComponentEvent createScaleEvent(Object source) {
        ComponentEvent event = new ComponentEvent(source);
        event.operateType = OperationEnum.SCALE.getType();
        event.setDescribe(OperationEnum.SCALE.getDescribe());
        return event;
    }

    public static ComponentEvent createConfigChangeEvent(Object source) {
        ComponentEvent event = new ComponentEvent(source);
        event.operateType = OperationEnum.CONFIG_CHANGE.getType();
        event.setDescribe(OperationEnum.CONFIG_CHANGE.getDescribe());
        return event;
    }

    public static ComponentEvent createRestartEvent(Object source) {
        ComponentEvent event = new ComponentEvent(source);
        event.operateType = OperationEnum.RESTART.getType();
        event.setDescribe(OperationEnum.RESTART.getDescribe());
        return event;
    }

    public static ComponentEvent createUpdateEvent(Object source) {
        ComponentEvent event = new ComponentEvent(source);
        event.operateType = OperationEnum.UPGRADE.getType();
        event.setDescribe(OperationEnum.UPGRADE.getDescribe());
        return event;
    }

}
