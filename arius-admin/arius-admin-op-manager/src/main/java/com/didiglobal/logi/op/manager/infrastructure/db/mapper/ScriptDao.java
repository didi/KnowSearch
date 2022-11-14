package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.ScriptPO;
import org.apache.ibatis.annotations.Param;
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
     *
     * @param id 脚本id
     * @return ScriptPO
     */
    ScriptPO findById(int id);

    /**
     * 通过name获取脚本po
     *
     * @param name 脚本名
     * @return ScriptPO
     */
    ScriptPO findByName(String name);

    /**
     * 查询script
     *
     * @param scriptPO 脚本
     * @return 脚本列表
     */
    List<ScriptPO> queryScript(ScriptPO scriptPO);

    /**
     * 插入数据
     *
     * @param scriptPO 脚本po
     * @return 自增id
     */
    int insert(ScriptPO scriptPO);

    /**
     * 更新脚本
     *
     * @param scriptPO 脚本po
     * @return 更新条数
     */
    int update(ScriptPO scriptPO);

    /**
     * 删除脚本
     *
     * @param id 脚本id
     * @return 删除条数
     */
    int delete(int id);

    /**
     * 分页查询脚本列表
     * @param scriptPO
     * @param from
     * @param size
     * @return
     */
    List<ScriptPO> pagingByCondition(@Param("param") ScriptPO scriptPO, @Param("from") Long from, @Param("size") Long size);

    /**
     * 查询脚本列表总数
     * @param scriptPO
     * @return
     */
    Long countByCondition(ScriptPO scriptPO);

    /**
     * 根据脚本id获取脚本
     * @param id
     * @return
     */
    ScriptPO findById(Long id);
}
