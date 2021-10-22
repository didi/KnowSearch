package com.didichuxing.datachannel.arius.admin.persistence.mysql.app;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppPO;

/**
 * @author d06679
 * @date 2019/3/13
 */
@Repository
public interface AppDAO {

    List<AppPO> listByCondition(AppPO param);

    int insert(AppPO param);

    AppPO getById(Integer appId);

    int update(AppPO param);

    int delete(Integer appId);

    List<AppPO> listByName(String name);

    List<AppPO> listByIds(List<Integer> appIds);

    List<AppPO> listByResponsible(String responsible);
}
