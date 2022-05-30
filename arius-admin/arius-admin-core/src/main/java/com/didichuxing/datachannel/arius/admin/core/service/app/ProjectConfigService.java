package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectConfigPO;
import java.util.List;
import java.util.Map;

public interface ProjectConfigService {
    /**
     * 获取porject id配置信息
     *
     * @param projectId APP ID
     * @return 配置信息
     */
    ProjectConfig getProjectConfig(int projectId);
    
    /**
     * listConfig
     *
     * @return List<App>
     */
    List<ProjectConfig> listConfig(List<Integer> projectIds);
    
    /**
     * 初始化project id配置
     * @param projectId projectId
     * @return 成功 true  失败false
     *
     */
    Result<Void> initConfig(Integer projectId);
    
    Map<Integer, ProjectConfig> projectId2ProjectConfigMap();
    
    Tuple<Result<Void>, ProjectConfigPO> updateProjectConfig(ProjectConfigDTO configDTO, String operator);
    
    Integer deleteByProjectId(int projectId);
    
}