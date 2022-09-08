package com.didiglobal.logi.op.manager.infrastructure.deployment;

import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.deployment.zeus.ZeusTaskStatus;

/**
 * @author didi
 * @date 2022-07-08 6:34 下午
 */
public interface DeploymentService {

    /**
     * 部署脚本
     * @param script
     * @return
     */
    Result<String> deployScript(Script script);

    /**
     * 修改脚本
     * @param script
     * @return
     */
    Result<Void> editScript(Script script);


    /**
     * 执行任务
     * @param templateId
     * @param hosts
     * @param action
     * @param batch
     * @param args
     * @return
     */
    Result<Integer> execute(String templateId, String hosts, String action, Integer batch, String... args);


    /**
     * 对任务id执行action操作
     * @param executeTaskId
     * @param action
     * @return
     */
    Result<Void> actionTask(int executeTaskId, String action);

    /**
     * 对任务host执行action操作
     * @param executeTaskId
     * @param host
     * @param action
     * @return
     */
    Result<Void> actionHost(int executeTaskId, String host, String action);

    /**
     * 执行状态
     * @param taskId
     * @return
     */
    Result<ZeusTaskStatus> deployStatus(int taskId);

    /**
     * 任务执行完成后的输出
     * @param taskId 任务id
     * @param hostname 主机名
     * @param taskLogEnumType 要获取的输出类型的type
     * @return
     */
    Result<String> deployTaskLog(int taskId, String hostname, int taskLogEnumType);

    /**
     * 移除脚本
     * @param script
     * @return
     */
    Result<Void> removeScript(Script script);
}
