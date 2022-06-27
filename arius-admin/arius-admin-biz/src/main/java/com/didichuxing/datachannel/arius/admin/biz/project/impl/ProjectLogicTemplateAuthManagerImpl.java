package com.didichuxing.datachannel.arius.admin.biz.project.impl;

import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.project.ProjectLogicTemplateAuthManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;

/**
 * Created by linyunan on 2021-06-15
 */
@Component
public class ProjectLogicTemplateAuthManagerImpl implements ProjectLogicTemplateAuthManager {

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;
    @Autowired
    private ProjectService                  projectService;

  

    @Override
    public List<ProjectTemplateAuth> getTemplateAuthListByTemplateListAndProjectId(Integer projectId,
                                                                                   List<IndexTemplate> indexTemplateList) {
        List<ProjectTemplateAuth> projectTemplateAuthList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(indexTemplateList)) {
            return projectTemplateAuthList;
        }

        if (!projectService.checkProjectExist(projectId)) {
            projectTemplateAuthList = indexTemplateList.stream()
                .map(r -> projectLogicTemplateAuthService.buildTemplateAuth(r, ProjectTemplateAuthEnum.NO_PERMISSION))
                .collect(Collectors.toList());
            return projectTemplateAuthList;
        }
        //判断是否为超级应用
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            projectTemplateAuthList = indexTemplateList.stream()
                .map(r -> projectLogicTemplateAuthService.buildTemplateAuth(r, ProjectTemplateAuthEnum.OWN))
                .collect(Collectors.toList());
            return projectTemplateAuthList;
        }

        List<ProjectTemplateAuth> appActiveTemplateRWAuths = projectLogicTemplateAuthService.getProjectActiveTemplateRWAndRAuths(
                projectId);
        Map<Integer, ProjectTemplateAuth> templateId2AppTemplateAuthMap = ConvertUtil.list2Map(appActiveTemplateRWAuths,
            ProjectTemplateAuth::getTemplateId);

        for (IndexTemplate indexTemplate : indexTemplateList) {
            Integer templateLogicId = indexTemplate.getId();
            if (null != projectId && projectId.equals(indexTemplate.getProjectId())) {
                projectTemplateAuthList.add(
                    projectLogicTemplateAuthService.buildTemplateAuth(indexTemplate, ProjectTemplateAuthEnum.OWN));
                continue;
            }

            if (null != templateLogicId && templateId2AppTemplateAuthMap.containsKey(templateLogicId)) {
                projectTemplateAuthList.add(templateId2AppTemplateAuthMap.get(templateLogicId));
                continue;
            }

            projectTemplateAuthList.add(projectLogicTemplateAuthService.buildTemplateAuth(indexTemplate,
                ProjectTemplateAuthEnum.NO_PERMISSION));
        }

        return projectTemplateAuthList;
    }

    @Override
    public Result<Void> updateTemplateAuth(ProjectTemplateAuthDTO authDTO, String operator) {
        if (AriusObjUtils.isNull(authDTO)) {
            return Result.buildFail("更新权限信息不存在");
        }

        if (AriusObjUtils.isNull(authDTO.getType())) {
            return Result.buildFail("更新权限类型不存在");
        }

        List<Integer> appTemplateAuthCodes = ProjectTemplateAuthEnum.listAppTemplateAuthCodes();
        if (!appTemplateAuthCodes.contains(authDTO.getType())) {
            return Result.buildFail("更新权限类型不支持");
        }

        ProjectTemplateAuth projectTemplateAuth = projectLogicTemplateAuthService
            .getTemplateRWAuthByLogicTemplateIdAndProjectId(authDTO.getTemplateId(), authDTO.getProjectId());

        if (AriusObjUtils.isNull(projectTemplateAuth)) {
            return Result.buildFail("权限信息不存在");
        }

        if (AriusObjUtils.isNull(projectTemplateAuth.getType())) {
            return Result.buildFail("权限信息不存在");
        }

        if (authDTO.getType().equals(projectTemplateAuth.getType())) {
            return Result.buildSucc();
        }

        projectTemplateAuth.setType(authDTO.getType());
        projectTemplateAuth.setResponsible(authDTO.getResponsible());
        return projectLogicTemplateAuthService
            .updateTemplateAuth(ConvertUtil.obj2Obj(projectTemplateAuth, ProjectTemplateAuthDTO.class), operator);
    }
}