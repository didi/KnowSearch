package com.didichuxing.datachannel.arius.admin.core.service.app.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectClusterPhyAuth;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterPhyAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectClusterPhyAuthService;

/**
 * Created by linyunan on 2021-10-15
 */
@Service
public class ProjectClusterPhyAuthServiceImpl implements ProjectClusterPhyAuthService {

    @Override
    public ProjectClusterPhyAuth buildClusterPhyAuth(Integer projectId, String clusterPhyName, AppClusterPhyAuthEnum appClusterPhyAuthEnum) {
        if (null == appClusterPhyAuthEnum || null == projectId || AriusObjUtils.isBlack(clusterPhyName)) {
            return null;
        }

        if (!AppClusterPhyAuthEnum.isExitByCode(appClusterPhyAuthEnum.getCode())) {
            return null;
        }

        ProjectClusterPhyAuth projectClusterPhyAuth = new ProjectClusterPhyAuth();
        projectClusterPhyAuth.setProjectId(projectId);
        projectClusterPhyAuth.setClusterPhyName(clusterPhyName);
        projectClusterPhyAuth.setType(appClusterPhyAuthEnum.getCode());
        return projectClusterPhyAuth;
    }
}