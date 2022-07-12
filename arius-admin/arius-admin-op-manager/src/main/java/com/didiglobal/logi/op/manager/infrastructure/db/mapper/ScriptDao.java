package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.ScriptPO;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-04 7:22 下午
 */
@Repository
public interface ScriptDao {

    /**
     * 通过id获取脚本po
     * @param id
     * @return
     */
    ScriptPO findById(int id);

    /**
     * 查询script
     * @param scriptPO
     * @return
     */
    List<ScriptPO> queryScript(ScriptPO scriptPO);

    /**
     * 插入数据
     * @param scriptPO
     */
    void insert(ScriptPO scriptPO);

    /**
     * 更新脚本
     * @param scriptPO
     */
    void update(ScriptPO scriptPO);

    /**
     * 删除脚本
     * @param id
     */
    void delete(int id);

}
