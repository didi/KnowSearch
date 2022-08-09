package com.didiglobal.logi.op.manager.domain.script.service.impl;

import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-04 7:07 下午
 */
public interface ScriptDomainService {

    /**
     * 根据id获取脚本
     * @param id
     * @return
     */
    Result<Script> getScriptById(int id);

    /**
     * 通过传入的脚本参数进行id查询以及名字和描述模糊匹配查询获取脚本
     * @param script
     * @return
     */
    Result<List<Script>> queryScript(Script script);

    /**
     * 创建脚本
     * @param script
     * @return
     */
    Result<Void> createScript(Script script);

    /**
     * 编辑脚本
     * @param script
     * @return
     */
    Result<Void> updateScript(Script script);

    /**
     * 删除脚本
     * @param id
     * @return
     */
    Result<Void> deleteScript(int id);
}
