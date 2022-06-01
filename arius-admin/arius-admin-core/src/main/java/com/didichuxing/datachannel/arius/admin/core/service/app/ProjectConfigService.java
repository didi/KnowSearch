package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectConfigPO;
import java.util.List;
import java.util.Map;

/**
 * 项目config服务 project config 每个项目都会初始化一个项目配置
 *
 * @author shizeying
 * @date 2022/06/01
 * @see com.didiglobal.logi.security.common.vo.project.ProjectVO
 */
public interface ProjectConfigService {
    /**
     * 获取项目config 获取project id配置信息
     *
     * @param projectId projectId
     * @return 配置信息
     */
    ProjectConfig getProjectConfig(int projectId);
    
    /**
     * 列表config 列表config listConfig
     *
     * @param projectIds 项目id
     * @return List<App>
     */
    List<ProjectConfig> listConfig(List<Integer> projectIds);
    
    /**
     * 项目id2下项目config地图
     * <p>
     * 项目id
     *
     * @return {@code Map<Integer, ProjectConfig>}
     */
    Map<Integer/*项目id*/, ProjectConfig> projectId2ProjectConfigMap();
    
    /**
     * 更新或初始化项目config 更新或初始化projectConfig
     *
     * @param configDTO configdto
     * @param operator  操作人
     * @return {@code Tuple<Result<Void>, ProjectConfigPO>}
     */
    Tuple<Result<Void>, ProjectConfigPO> updateOrInitProjectConfig(ProjectConfigDTO configDTO, String operator);
    
    /**
     * 按项目id删除 通过项目id逻辑删除项目配置
     *
     * @param projectId 项目id
     */
    void deleteByProjectId(int projectId);
    
}