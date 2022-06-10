package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.ProjectExtendManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectExtendSaveDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectBriefExtendVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectConfigVo;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectExtendVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.security.common.Result;
import com.didiglobal.logi.security.common.dto.project.ProjectSaveDTO;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.util.CopyBeanUtil;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * logi 项目的扩展能力 将logi中整体能力全部移动到这里
 *
 * @author shizeying
 * @date 2022/06/10
 */
@Component
public class ProjectExtendManagerImpl implements ProjectExtendManager {
    
    @Autowired
    private ProjectConfigService projectConfigService;
    @Autowired
    private ProjectService       projectService;
    @Autowired
    private ESUserService        esUserService;
    
    @Autowired
    private ClusterLogicService  clusterLogicService;
    @Autowired
    private IndexTemplateService indexTemplateService;
    
    @Override
    public Result<ProjectExtendVO> createProject(ProjectExtendSaveDTO saveDTO, String operator) {
        try {
            
            ProjectSaveDTO project = saveDTO.getProject();
            ProjectConfigDTO config = saveDTO.getConfig();
            ProjectVO projectVO = projectService.createProject(project, operator);
            ProjectExtendVO projectExtendVO = CopyBeanUtil.copy(project, ProjectExtendVO.class);
            config.setProjectId(projectVO.getId());
            Tuple<com.didichuxing.datachannel.arius.admin.common.bean.common.Result<Void>, ProjectConfigPO> resultProjectConfigPOTuple = projectConfigService.updateOrInitProjectConfig(
                    config, operator);
            //todo 需要设置创建成功的日志
            if (resultProjectConfigPOTuple.getV1().success()) {
                if (AuthConstant.SUPER_PROJECT_ID.equals(projectExtendVO.getId())) {
                    projectExtendVO.setIsAdmin(true);
                } else {
                    projectExtendVO.setIsAdmin(false);
                }
                projectExtendVO.setConfig(CopyBeanUtil.copy(resultProjectConfigPOTuple.getV2(), ProjectConfigVo.class));
                
            }
            
            //创建es user
            createESUserDefault(projectVO, operator);
            return Result.<ProjectExtendVO>buildSucc(projectExtendVO);
        } catch (LogiSecurityException e) {
            e.printStackTrace();
            return com.didiglobal.logi.security.common.Result.fail(e);
        }
    }
    
    @Override
    public Result<ProjectExtendVO> getProjectDetailByProjectId(Integer projectId) {
        try {
            ProjectVO projectVO = projectService.getProjectDetailByProjectId(projectId);
            ProjectExtendVO projectExtendVO = CopyBeanUtil.copy(projectVO, ProjectExtendVO.class);
            if (AuthConstant.SUPER_PROJECT_ID.equals(projectExtendVO.getId())) {
                projectExtendVO.setIsAdmin(true);
            } else {
                projectExtendVO.setIsAdmin(false);
            }
            ProjectConfig projectConfig = projectConfigService.getProjectConfig(projectId);
            projectExtendVO.setConfig(CopyBeanUtil.copy(projectConfig, ProjectConfigVo.class));
            
            return Result.<ProjectExtendVO>buildSucc(projectExtendVO);
        } catch (LogiSecurityException e) {
            return com.didiglobal.logi.security.common.Result.fail(e);
        }
    }
    
    @Override
    public Result<List<ProjectBriefExtendVO>> getProjectBriefList() {
        List<ProjectBriefVO> projectBriefList = projectService.getProjectBriefList();
        List<ProjectBriefExtendVO> projectBriefExtendList = ConvertUtil.list2List(projectBriefList,
                ProjectBriefExtendVO.class);
        return getListResult(projectBriefExtendList);
    }
    
    @Override
    public com.didichuxing.datachannel.arius.admin.common.bean.common.Result<Void> deleteProjectByProjectId(
            Integer projectId, String operator) {
        //项目删除前的检查
        //校验项目是否绑定es user
        List<ESUser> esUsers = esUserService.listESUsers(Collections.singletonList(projectId));
        if (CollectionUtils.isNotEmpty(esUsers)) {
            return com.didichuxing.datachannel.arius.admin.common.bean.common.Result.buildFail("项目已绑定es user，不能删除");
        }
        //校验项目绑定逻辑集群
        List<ClusterLogic> clusterLogics = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
        if (CollectionUtils.isNotEmpty(clusterLogics)) {
            return com.didichuxing.datachannel.arius.admin.common.bean.common.Result.buildFail("项目已绑定逻辑集群，不能删除");
        }
        
        //校验项目绑定模板服务
        List<IndexTemplate> indexTemplates = indexTemplateService.getProjectLogicTemplatesByProjectId(projectId);
        if (CollectionUtils.isNotEmpty(indexTemplates)) {
            return com.didichuxing.datachannel.arius.admin.common.bean.common.Result.buildFail("项目已绑定模板服务，不能删除");
        }
        
        projectService.deleteProjectByProjectId(projectId, operator);
        return com.didichuxing.datachannel.arius.admin.common.bean.common.Result.buildSucc();
    }
    
    @NotNull
    private Result<List<ProjectBriefExtendVO>> getListResult(List<ProjectBriefExtendVO> projectBriefExtendList) {
        for (ProjectBriefExtendVO projectBriefExtendVO : projectBriefExtendList) {
            if (AuthConstant.SUPER_PROJECT_ID.equals(projectBriefExtendVO.getId())) {
                projectBriefExtendVO.setIsAdmin(true);
            } else {
                projectBriefExtendVO.setIsAdmin(false);
            }
            ProjectConfig projectConfig = projectConfigService.getProjectConfig(projectBriefExtendVO.getId());
            projectBriefExtendVO.setConfig(CopyBeanUtil.copy(projectConfig, ProjectConfigVo.class));
        }
        return Result.buildSucc(projectBriefExtendList);
    }
    
    /**
     * 新增默认的es user
     *
     * @param data 数据
     */
    private void createESUserDefault(ProjectVO data, String operator) {
        ESUserDTO esUserDTO = new ESUserDTO();
        esUserDTO.setIsRoot(0);
        esUserDTO.setSearchType(AppSearchTypeEnum.TEMPLATE.getCode());
        esUserDTO.setVerifyCode(RandomStringUtils.randomAlphabetic(7));
        esUserDTO.setMemo(((ProjectVO) data).getProjectName() + "项目默认的es user");
        esUserDTO.setProjectId(((ProjectVO) data).getId());
        esUserService.registerESUser(esUserDTO, operator);
    }
}