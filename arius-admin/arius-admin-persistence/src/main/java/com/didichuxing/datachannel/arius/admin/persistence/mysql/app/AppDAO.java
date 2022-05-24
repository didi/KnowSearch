package com.didichuxing.datachannel.arius.admin.persistence.mysql.app;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppPO;

/**
 * @author linyunan
 * @date 2021-04-28
 * @deprecated 引入多租户体系后续进行下线删除
 */
@Repository
@Deprecated
public interface AppDAO {

    /**
     * 条件查询
     * @param  param   param
     * @return         List<AppPO>
     */
    List<AppPO> listByCondition(AppPO param);

    /**
     * 插入
     * @param param param
     * @return      插入成功条数
     */
    int insert(AppPO param);

    /**
     * 根据id获取App
     * @param appId    主键
     * @return         AppPO
     */
    AppPO getById(Integer appId);

    /**
     * 更新
     * @param param   param
     * @return   更新成功条数
     */
    int update(AppPO param);

    /**
     * 删除
     * @param appId  主键
     * @return   删除成功条数
     */
    int delete(Integer appId);

    /**
     * 根据app名称获取app信息
     * @param name  app名称
     * @return      List<AppPO>
     */
    List<AppPO> listByName(String name);

    /**
     * 根据主键列表获取app信息
     * @param appIds    主键列表
     * @return           List<AppPO>
     */
    List<AppPO> listByIds(List<Integer> appIds);

    /**
     * 根据责任人获取关联的app列表信息
     * @param responsible    责任人
     * @return               List<AppPO>
     */
    List<AppPO> listByResponsible(String responsible);
}