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

}
