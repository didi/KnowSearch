package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterPhyAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;

import java.util.List;

/**
 * @author linyunan
 * @date 2021-04-28
 */
public interface AppClusterPhyAuthManager {
    /**
     * 从缓存中获取当前项目对物理集群列表的权限信息
     * @param appId                    项目
     * @param clusterPhyList           物理集群信息列表
     * @return                         List<AppClusterPhyAuth>
     */
    List<AppClusterPhyAuth> getByClusterPhyListAndAppIdFromCache(Integer appId, List<ClusterPhy> clusterPhyList);


    /**
     * 获取项目有访问权限的物理集群列表信息
     * @param appId                项目
     * @return                      List<AppClusterPhyAuth>
     */
    List<AppClusterPhyAuth> getAppAccessClusterPhyAuths(Integer appId);
}
