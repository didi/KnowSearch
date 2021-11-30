package com.didichuxing.datachannel.arius.admin.biz.app;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;

/**
 * Created by linyunan on 2021-10-17
 */

public interface AppClusterLogicAuthManager {

    /**
     * 获取当前项目对逻辑集群列表的权限信息
     * @param appId                    项目
     * @param clusterLogicList         逻辑集群信息列表
     * @return
     */
    List<AppClusterLogicAuth> getByClusterLogicListAndAppId(Integer appId, List<ClusterLogic> clusterLogicList);
}
