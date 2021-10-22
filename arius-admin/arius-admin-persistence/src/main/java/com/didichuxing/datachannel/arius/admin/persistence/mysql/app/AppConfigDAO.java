package com.didichuxing.datachannel.arius.admin.persistence.mysql.app;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppConfigPO;

/**
 * @author d06679
 * @date 2019/3/13
 */
@Repository
public interface AppConfigDAO {

    AppConfigPO getByAppId(int appId);

    int insert(AppConfigPO param);

    int update(AppConfigPO param);

    List<AppConfigPO> listAll();
}
