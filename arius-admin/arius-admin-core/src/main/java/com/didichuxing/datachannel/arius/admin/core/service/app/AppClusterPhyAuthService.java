package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterPhyAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterPhyAuth;

/**
 * Created by linyunan on 2021-10-15
 */
public interface AppClusterPhyAuthService {

    /**
     * 构建项目对物理集群的权限信息
     * @param appId                  项目
     * @param clusterPhyName         物理集群名称
     * @param appClusterPhyAuthEnum  权限点
     * @return
     */
    AppClusterPhyAuth buildClusterPhyAuth(Integer appId, String clusterPhyName,
                                          AppClusterPhyAuthEnum appClusterPhyAuthEnum);
}
