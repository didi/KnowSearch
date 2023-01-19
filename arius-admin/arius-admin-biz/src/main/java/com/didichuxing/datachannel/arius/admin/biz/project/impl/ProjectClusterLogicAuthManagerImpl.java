package com.didichuxing.datachannel.arius.admin.biz.project.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.didichuxing.datachannel.arius.admin.biz.project.ProjectClusterLogicAuthManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectClusterLogicAuthService;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.google.common.collect.Lists;

/**
 * @author linyunan
 * @date 2021-04-28
 */
@Component
public class ProjectClusterLogicAuthManagerImpl implements ProjectClusterLogicAuthManager {
    @Autowired
    private ProjectService                 projectService;

    @Autowired
    private ProjectClusterLogicAuthService projectClusterLogicAuthService;

    @Override
    public List<ProjectClusterLogicAuth> getByClusterLogicListAndProjectId(Integer projectId,
                                                                           List<ClusterLogic> clusterLogicList) {
        List<ProjectClusterLogicAuth> projectClusterLogicAuthList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(clusterLogicList)) {
            return projectClusterLogicAuthList;
        }

        if (!projectService.checkProjectExist(projectId)) {
            projectClusterLogicAuthList = clusterLogicList.stream().map(r -> projectClusterLogicAuthService
                .buildClusterLogicAuth(projectId, r.getId(), ProjectClusterLogicAuthEnum.NO_PERMISSIONS))
                .collect(Collectors.toList());
            return projectClusterLogicAuthList;
        }

        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            projectClusterLogicAuthList = clusterLogicList.stream().map(r -> projectClusterLogicAuthService
                .buildClusterLogicAuth(projectId, r.getId(), ProjectClusterLogicAuthEnum.OWN))
                .collect(Collectors.toList());
            return projectClusterLogicAuthList;
        }

        projectClusterLogicAuthList = clusterLogicList.stream()
            .map(clusterLogic -> projectClusterLogicAuthService.getLogicClusterAuth(projectId, clusterLogic.getId()))
            .collect(Collectors.toList());

        //处理无权限
        for (ProjectClusterLogicAuth projectClusterLogicAuth : projectClusterLogicAuthList) {
            if (null == projectClusterLogicAuth.getType()) {
                projectClusterLogicAuth.setType(ProjectClusterLogicAuthEnum.NO_PERMISSIONS.getCode());
            }
        }
        return projectClusterLogicAuthList;
    }
}