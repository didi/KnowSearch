package com.didichuxing.datachannel.arius.admin.persistence.mysql.app;

import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * es 用户
 *
 * @author shizeying
 * @date 2022/05/25
 */
@Repository
public interface ESUserDAO {

    /**
     * 条件查询
     * @param  param   param
     * @return         List<AppPO>
     */
    List<ESUserPO> listByCondition(ESUserPO param);

    /**
     * 插入
     * @param param param
     * @return      插入成功条数
     */
    int insert(ESUserPO param);
    
    int countByProjectId(@Param("projectId")Integer projectId);

    /**
     * 根据id获取App
     * @param esUser    主键
     * @return         AppPO
     */
    ESUserPO getByESUser(@Param("esUser") Integer esUser);

    /**
     * 更新
     * @param param   param
     * @return   更新成功条数
     */
    int update(ESUserPO param);

    /**
     * 删除
     * @param esUser  主键
     * @return   删除成功条数
     */
    int delete(@Param("esUser") Integer esUser);
    
    /**
     * 删除项目中所有的es user
     *
     * @param projectId 项目id
     * @return int
     */
    int deleteByProjectId(@Param("projectId") Integer projectId);

    /**
     * 根据项目id获取app信息
     * @param projectId  项目名称
     * @return      List<AppPO>
     */
    List<ESUserPO> listByProjectId(@Param("projectId")Integer projectId);

    /**
     * 根据主键列表获取app信息
     * @param esUsers    主键列表
     * @return           List<AppPO>
     */
    List<ESUserPO> listByESUsers(@Param("esUser") List<Integer> esUsers);
        /**
     * 根据责任人获取关联的app列表信息
     * @param responsible    责任人
     * @return               List<AppPO>
     */
    List<ESUserPO> listByResponsible(String responsible);


}