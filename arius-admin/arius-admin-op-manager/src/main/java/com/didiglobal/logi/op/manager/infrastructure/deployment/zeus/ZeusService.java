package com.didiglobal.logi.op.manager.infrastructure.deployment.zeus;

import com.alibaba.fastjson.JSONObject;
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
     * @param zeusTemplate 模板
     * @return
     * @throws ZeusOperationException
     */
    void editTemplate(ZeusTemplate zeusTemplate) throws ZeusOperationException;

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


    /**
     * 对任务进行操作
     * @param param
     * @throws ZeusOperationException
     */
    void actionTask(JSONObject param) throws ZeusOperationException;

    /**
     * 对host进行操作
     * @param param
     * @throws ZeusOperationException
     */
    void actionHost(JSONObject param) throws ZeusOperationException;
}
