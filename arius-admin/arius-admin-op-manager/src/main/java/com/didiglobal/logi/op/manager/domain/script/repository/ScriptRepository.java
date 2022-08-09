package com.didiglobal.logi.op.manager.domain.script.repository;

import com.didiglobal.logi.op.manager.domain.script.entity.Script;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-04 7:07 下午
 */
public interface ScriptRepository {
    /**
     * 根据id获取脚本
     * @param id
     * @return Script
     */
    Script findById(int id);

    /**
     * 根据name获取脚本
     * @param name
     * @return Script
     */
    Script findByName(String name);

    /**
     * 获取脚本list
     * @param script
     * @return List<Script>
     */
    List<Script> queryScript(Script script);

    /**
     * 更新脚本，脚本内容以及描述
     * @param script
     */
    void updateScript(Script script);

    /**
     * 新增
     * @param script
     */
    void insertScript(Script script);

    /**
     * 删除脚本
     * @param id
     */
    void deleteScript(int id);
}
