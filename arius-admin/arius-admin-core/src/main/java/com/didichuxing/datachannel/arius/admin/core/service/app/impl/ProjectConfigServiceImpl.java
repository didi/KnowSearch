package com.didichuxing.datachannel.arius.admin.core.service.app.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.yesOrNo;
import static com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil.obj2Obj;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ProjectConfigDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectConfigServiceImpl implements ProjectConfigService {
    private static final ILog             LOGGER = LogFactory.getLog(ESUserServiceImpl.class);
    @Autowired
    private              ProjectConfigDAO projectConfigDAO;
    
    /**
     * 获取porject id配置信息
     *
     * @param projectId APP ID
     * @return 配置信息
     */
    @Override
    public ProjectConfig getProjectConfig(int projectId) {
        
        ProjectConfigPO oldConfigPO = projectConfigDAO.getByProjectId(projectId);
        if (oldConfigPO == null) {
            initConfig(projectId);
            oldConfigPO = projectConfigDAO.getByProjectId(projectId);
            
        }
        
        return obj2Obj(oldConfigPO, ProjectConfig.class);
    }
    
    /**
     * listConfig
     *
     * @param projectIds
     * @return List<App>
     */
    @Override
    public List<ProjectConfig> listConfig(List<Integer> projectIds) {
        return null;
    }
    
    /**
     * 初始化project id配置
     *
     * @param projectId projectId
     * @return 成功 true  失败false
     */
    @Override
    public Result<Void> initConfig(Integer projectId) {
        
        ProjectConfigPO param = new ProjectConfigPO();
        param.setProjectId(projectId);
        param.setDslAnalyzeEnable(AdminConstant.YES);
        param.setIsSourceSeparated(AdminConstant.NO);
        param.setAggrAnalyzeEnable(AdminConstant.YES);
        param.setAnalyzeResponseEnable(AdminConstant.YES);
        return Result.build(projectConfigDAO.update(param) == 1);
    }
    
    /**
     *
     * @return
     */
    @Override
    public Map<Integer, ProjectConfig> projectId2ProjectConfigMap() {
        return null;
    }
    
    /**
     * 修改APP配置
     *
     * @param configDTO 配置信息
     * @param operator  操作人
     * @return 成功 true  失败  false
     * <p>
     * NotExistException APP不存在 IllegalArgumentException 参数不合理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tuple<Result<Void>, ProjectConfigPO> updateProjectConfig(ProjectConfigDTO configDTO, String operator) {
        Result<Void> checkResult = checkConfigParam(configDTO);
        if (checkResult.failed()) {
            LOGGER.warn("class=ProjectConfigServiceImpl||method=updateProjectConfig||msg={}||msg=check fail!",
                    checkResult.getMessage());
            return new Tuple<>(checkResult, null);
        }
        if (projectConfigDAO.checkProjectConfigByProjectId(configDTO.getProjectId())) {
            ProjectConfigPO oldConfigPO = projectConfigDAO.getByProjectId(configDTO.getProjectId());
            boolean succ = (1 == projectConfigDAO.update(obj2Obj(configDTO, ProjectConfigPO.class)));
            return new Tuple<>(Result.build(succ), oldConfigPO);
            
        } else {
            boolean succ = (1 == projectConfigDAO.update(obj2Obj(configDTO, ProjectConfigPO.class)));
            final ProjectConfigPO newProjectConfigPO = projectConfigDAO.getByProjectId(configDTO.getProjectId());
            
            return new Tuple<>(Result.build(succ), newProjectConfigPO);
        }
        
    }
    
    /**
     *
     * @param projectId
     * @return
     */
    @Override
    public Integer deleteByProjectId(int projectId) {
        return projectConfigDAO.deleteByProjectId(projectId);
    }
    
    private Result<Void> checkConfigParam(ProjectConfigDTO configDTO) {
        if (configDTO == null) {
            return Result.buildParamIllegal("配置信息为空");
        }
        if (configDTO.getProjectId() == null) {
            return Result.buildParamIllegal("应用ID为空");
        }
        if (configDTO.getAnalyzeResponseEnable() != null && !yesOrNo(configDTO.getAnalyzeResponseEnable())) {
            return Result.buildParamIllegal("解析响应结果开关非法");
        }
        if (configDTO.getDslAnalyzeEnable() != null && !yesOrNo(configDTO.getDslAnalyzeEnable())) {
            return Result.buildParamIllegal("DSL分析开关非法");
        }
        if (configDTO.getAggrAnalyzeEnable() != null && !yesOrNo(configDTO.getAggrAnalyzeEnable())) {
            return Result.buildParamIllegal("聚合分析开关非法");
        }
        if (configDTO.getIsSourceSeparated() != null && !yesOrNo(configDTO.getIsSourceSeparated())) {
            return Result.buildParamIllegal("索引存储分离开关非法");
        }
        
        return Result.buildSucc();
    }
}