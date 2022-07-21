package com.didiglobal.logi.op.manager.infrastructure.deployment;

import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;

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
    Result<String> editScript(Script script);


    /**
     * 执行
     * @param templateId
     * @param taskId
     * @param groupName
     * @param hosts
     * @return
     */
    Result<Integer> execute(String templateId, Integer taskId, String groupName, String hosts, String... args);

    /**
     * 执行状态
     * @param taskId
     * @return
     */
    Result<ZeusTaskStatus> deployStatus(int taskId);
}
