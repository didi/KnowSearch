package com.didichuxing.datachannel.arius.admin.core.service.project.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.yesOrNo;
import static com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil.obj2Obj;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.project.ProjectConfigDAO;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 *
 *
 * @author shizeying
 * @date 2022/06/01
 */
@Service
public class ProjectConfigServiceImpl implements ProjectConfigService {
    private static final ILog LOGGER = LogFactory.getLog(ESUserServiceImpl.class);
    @Autowired
    private ProjectConfigDAO  projectConfigDAO;

    /**
     * 获取porject id配置信息
     *
     * @param projectId APP ID
     * @return 配置信息
     */
    @Override
    public ProjectConfig getProjectConfig(int projectId) {
        return obj2Obj(projectConfigDAO.getByProjectId(projectId), ProjectConfig.class);
    }

    /**
     * @return
     */
    @Override
    public Map<Integer, ProjectConfig> projectId2ProjectConfigMap() {
        return projectConfigDAO.listAll().stream().collect(Collectors.toMap(ProjectConfigPO::getProjectId,
            projectConfigPO -> ConvertUtil.obj2Obj(projectConfigPO, ProjectConfig.class)));
    }

    /**
     * 修改APP配置
     *
     * @param configDTO 配置信息
     * @param operator  操作人
     * @return 成功 true  失败  false
     * <p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TupleTwo<Result<Void>, ProjectConfigPO> updateOrInitProjectConfig(ProjectConfigDTO configDTO,
                                                                             String operator) {
        Result<Void> checkResult = checkConfigParam(configDTO);
        if (checkResult.failed()) {
            LOGGER.warn("class=ProjectConfigServiceImpl||method=updateProjectConfig||msg={}||msg=check fail!",
                checkResult.getMessage());
            return Tuples.of(checkResult, null);
        }
        //当项目存在的时候
        if (projectConfigDAO.checkProjectConfigByProjectId(configDTO.getProjectId())) {
            ProjectConfigPO oldConfigPO = projectConfigDAO.getByProjectId(configDTO.getProjectId());
            boolean succ = (1 == projectConfigDAO.update(obj2Obj(configDTO, ProjectConfigPO.class)));
            return Tuples.of(Result.build(succ), oldConfigPO);

        }
        //
        else {
            ProjectConfigPO param = obj2Obj(configDTO, ProjectConfigPO.class);
            if (param.getDslAnalyzeEnable() == null) {
                param.setDslAnalyzeEnable(AdminConstant.YES);

            }
            if (param.getIsSourceSeparated() == null) {
                param.setIsSourceSeparated(AdminConstant.NO);

            }
            if (param.getAggrAnalyzeEnable() == null) {
                param.setAggrAnalyzeEnable(AdminConstant.YES);

            }
            if (param.getAnalyzeResponseEnable() == null) {
                param.setAnalyzeResponseEnable(AdminConstant.YES);

            }
            boolean succ = (1 == projectConfigDAO.insert(param));
            final ProjectConfigPO newProjectConfigPO = projectConfigDAO.getByProjectId(configDTO.getProjectId());

            return Tuples.of(Result.build(succ), newProjectConfigPO);
        }

    }

    /**
     * @param projectId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByProjectId(int projectId) {
        if (projectConfigDAO.checkProjectConfigByProjectId(projectId)) {
            projectConfigDAO.deleteByProjectId(projectId);
        }
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