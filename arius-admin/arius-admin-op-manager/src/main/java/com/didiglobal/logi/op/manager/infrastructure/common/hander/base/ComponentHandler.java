package com.didiglobal.logi.op.manager.infrastructure.common.hander.base;

import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.infrastructure.common.ProcessStatus;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;

/**
 * @author didi
 * @date 2022-07-16 2:08 下午
 */
public interface ComponentHandler {
    /**
     * 事件处理
     * @param componentEvent
     * @throws ComponentHandlerException
     */
    void eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException;

    /**
     * 任务执行
     * @param task
     * @return
     * @throws ComponentHandlerException
     */
    Result<Void> execute(Task task);

    /**
     * 获取进度
     * @param task
     * @param <T>
     * @return
     * @throws ComponentHandlerException
     */
    <T extends ProcessStatus> T getProcessStatus(Task task) throws ComponentHandlerException;

    /**
     * 任务完成后的处理
     * @param content
     * @throws ComponentHandlerException
     */
    void taskFinishProcess(String content) throws ComponentHandlerException;;

    /**
     * 处理器类型
     * @return
     */
    Integer getOperationType();

}


