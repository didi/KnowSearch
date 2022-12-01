package com.didichuxing.datachannel.arius.admin.biz.project.impl;

import com.didichuxing.datachannel.arius.admin.biz.project.ProjectLogicTemplateAuthManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectTemplateAuthVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.knowframework.security.service.ProjectService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by linyunan on 2021-06-15
 */
@Component
public class ProjectLogicTemplateAuthManagerImpl implements ProjectLogicTemplateAuthManager {

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;
    @Autowired
    private ProjectService                  projectService;
    @Autowired
    private OperateRecordService            operateRecordService;
    @Autowired
    private IndexTemplateService            indexTemplateService;

    /**
     * @param authDTO
     * @param operator
     * @param projectId
     * @return
     */
    @Override
    public Result<Void> addTemplateAuth(ProjectTemplateAuthDTO authDTO, String operator, Integer projectId) {
        Result<Void> checkProjectCorrectly = ProjectUtils.checkProjectCorrectly(ProjectTemplateAuthDTO::getProjectId,
            authDTO, projectId);
        if (checkProjectCorrectly.failed()) {
            return checkProjectCorrectly;
        }

        Result<Void> voidResult = projectLogicTemplateAuthService.addTemplateAuth(authDTO);
        if (voidResult.success()) {
            final ProjectTemplateAuthEnum projectTemplateAuthEnum = ProjectTemplateAuthEnum.valueOf(authDTO.getType());
            operateRecordService.saveOperateRecordWithManualTrigger(
                    String.format("权限变更：%s", projectTemplateAuthEnum.getDesc()), operator, projectId,
                    authDTO.getId(), OperateTypeEnum.TEMPLATE_MANAGEMENT_INFO_MODIFY);

        }
        return voidResult;
    }

    /**
     * @param delete
     * @return
     */
    @Override
    public Result<Void> deleteRedundancyTemplateAuths(boolean delete) {
        return Result.build(projectLogicTemplateAuthService.deleteRedundancyTemplateAuths(true));
    }

    /**
     * 得到app模板身份验证
     *
     * @param projectId 项目id
     * @return {@link Result}<{@link List}<{@link ProjectTemplateAuthVO}>>
     */
    @Override
    public Result<List<ProjectTemplateAuthVO>> getProjectTemplateAuths(Integer projectId) {
        List<ProjectTemplateAuthVO> templateAuths = ConvertUtil.list2List(
            projectLogicTemplateAuthService.getProjectActiveTemplateRWAndRAuths(projectId),
            ProjectTemplateAuthVO.class);

        fillTemplateAuthVO(templateAuths);
        return Result.buildSucc(templateAuths);
    }

    /**
     * @param authId
     * @param operator
     * @param projectId
     * @return
     */
    @Override
    public Result<Void> deleteTemplateAuth(Long authId, String operator, Integer projectId) {
        Integer belongToProject = projectLogicTemplateAuthService.getProjectIdById(authId);
        Result<Void> checkProjectCorrectly = ProjectUtils.checkProjectCorrectly(i -> i, belongToProject, projectId);
        if (checkProjectCorrectly.failed()) {
            return checkProjectCorrectly;
        }
        Result<Void> result = projectLogicTemplateAuthService.deleteTemplateAuth(authId);
        if (result.success()) {
    
            operateRecordService.saveOperateRecordWithManualTrigger(String.format("删除模板，模板 id：%s", authId),
                    operator, projectId, authId, OperateTypeEnum.TEMPLATE_MANAGEMENT_OFFLINE);
        }
        return result;
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
        final Result<Void> result = projectLogicTemplateAuthService.updateTemplateAuth(
                ConvertUtil.obj2Obj(projectTemplateAuth, ProjectTemplateAuthDTO.class));
        if (result.success()) {
            final ProjectTemplateAuthEnum projectTemplateAuthEnum = ProjectTemplateAuthEnum.valueOf(authDTO.getType());
    
            operateRecordService.saveOperateRecordWithManualTrigger(
                    String.format("权限变更：【%s】", projectTemplateAuthEnum.getDesc()), operator, authDTO.getProjectId(),
                    authDTO.getId(), OperateTypeEnum.TEMPLATE_MANAGEMENT_INFO_MODIFY);
        }
        return result;
    
    }

    /**
    * 给AppTemplateAuthVO设置所属逻辑集群ID、name，逻辑模板name
    * @param templateAuths 模板权限列表
    */
    private void fillTemplateAuthVO(List<ProjectTemplateAuthVO> templateAuths) {
        if (CollectionUtils.isEmpty(templateAuths)) {
            return;
        }

        // 涉及的逻辑模板id
        List<Integer> templateIds = templateAuths.stream().map(ProjectTemplateAuthVO::getTemplateId)
            .collect(Collectors.toList());

        Map<Integer, IndexTemplateLogicWithClusterAndMasterTemplate> logicTemplateMap = indexTemplateService
            .getLogicTemplatesWithClusterAndMasterTemplateMap(new HashSet<>(templateIds));

        for (ProjectTemplateAuthVO authVO : templateAuths) {
            Integer templateId = authVO.getTemplateId();
            IndexTemplateLogicWithClusterAndMasterTemplate logicTemplate = logicTemplateMap.get(templateId);
            if (logicTemplate != null) {
                // 逻辑模板信息
                authVO.setTemplateName(logicTemplate.getName());
                // 逻辑集群信息
                ClusterLogic logicCluster = logicTemplate.getLogicCluster();
                // 物理模板被删除后有可能没有集群信息
                if (logicCluster != null) {
                    authVO.setLogicClusterId(logicCluster.getId());
                    authVO.setLogicClusterName(logicCluster.getName());
                } else {
                    authVO.setLogicClusterName("");
                }
            } else {
                authVO.setTemplateName("");
            }
        }
    }
}