package com.didiglobal.logi.op.manager.infrastructure.deployment;

import com.didiglobal.logi.op.manager.infrastructure.exception.ZeusOperationException;

/**
 * @author didi
 * @date 2022-07-08 6:34 下午
 */
public interface ZeusService {

    /**
     * 创建模板
     * @param zeusTemplate
     * @return
     * @throws ZeusOperationException
     */
    String createTemplate(ZeusTemplate zeusTemplate) throws ZeusOperationException;

    /**
     * 修改模板
     * @param zeusTemplate
     * @return
     * @throws ZeusOperationException
     */
    String editTemplate(ZeusTemplate zeusTemplate) throws ZeusOperationException;

    /**
     * 执行任务
     * @param zeusTask
     * @return
     * @throws ZeusOperationException
     */
    Integer executeTask(ZeusTask zeusTask) throws ZeusOperationException;

    /**
     * 任务状态
     * @param taskId
     * @return
     * @throws ZeusOperationException
     */
    ZeusTaskStatus getTaskStatus(int taskId) throws ZeusOperationException;

    /**
     * 删除模板
     * @param templateId
     * @throws ZeusOperationException
     */
    void deleteTemplate(int templateId) throws ZeusOperationException;
}
