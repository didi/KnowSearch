package com.didiglobal.logi.op.manager.domain.component.event;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.event.DomainEvent;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.TIME_WAIT;

/**
 * @author didi
 * @date 2022-07-12 4:25 下午
 */
public class ComponentEvent extends DomainEvent<Result> {
    /**
     * 操作类型
     */
    private int operateType;

    private Result result;

    public int getOperateType() {
        return operateType;
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

    public static ComponentEvent createExecuteFunctionEvent(Object source) {
        ComponentEvent event = new ComponentEvent(source);
        event.operateType = OperationEnum.FUNCTION_EXECUTE.getType();
        event.setDescribe(OperationEnum.FUNCTION_EXECUTE.getDescribe());
        return event;
    }

    public static ComponentEvent createRollbackEvent(Object source) {
        ComponentEvent event = new ComponentEvent(source);
        event.operateType = OperationEnum.ROLLBACK.getType();
        event.setDescribe(OperationEnum.ROLLBACK.getDescribe());
        return event;
    }

    @Override
    public synchronized Result getResult() {
        try {
            if (null == result) {
                this.wait(TIME_WAIT);
            }
            //判断到底是超时还是唤醒
            if (null == result) {
                return Result.fail("任务事件超时");
            } else {
                return result;
            }
        } catch (InterruptedException e) {
            return Result.fail("任务事件中断");
        }

    }

    @Override
    public synchronized void setValue(Result result) {
        this.result = result;
        this.notify();
    }
}
