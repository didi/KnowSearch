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
    String ditTemplate(ZeusTemplate zeusTemplate) throws ZeusOperationException;
}
