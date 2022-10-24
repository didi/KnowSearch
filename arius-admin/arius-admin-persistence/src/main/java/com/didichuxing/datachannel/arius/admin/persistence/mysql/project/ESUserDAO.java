package com.didichuxing.datachannel.arius.admin.persistence.mysql.project;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ESUserPO;
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

    int countByProjectId(@Param("projectId") Integer projectId);

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
    List<ESUserPO> listByProjectId(@Param("projectId") Integer projectId);

    /**
     * 根据主键列表获取app信息
     * @param projectIds    项目集合
     * @return           List<AppPO>
     */
    List<ESUserPO> listByProjectIds(@Param("projectIds") List<Integer> projectIds);



    Integer maxById();

    /**
     * 通过项目id获取默认的es user
     *
     * @param projectId 项目id
     * @return {@code ESUser}
     */
    ESUser getDefaultESUserByProject(@Param("projectId") Integer projectId);

    /**
     * 获取项目中的默认es user
     *
     * @param projectId 项目id
     * @return int
     */
    int countDefaultESUserByProject(@Param("projectId") Integer projectId);

    /**
     * 获取项目id通过搜索类型
     *
     * @param searchType 搜索类型
     * @return {@code List<Integer>}
     */
    List<Integer> getProjectIdBySearchType(@Param("searchType") Integer searchType);

    /**
     * 通过项目id和搜索类型
     *
     * @param searchType 搜索类型
     * @param projectId  项目id
     * @return int
     */
    int countByProjectIdAndSearchType(@Param("searchType") Integer searchType, @Param("projectId") Integer projectId);

}