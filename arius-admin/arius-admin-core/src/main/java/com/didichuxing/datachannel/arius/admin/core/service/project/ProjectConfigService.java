package com.didichuxing.datachannel.arius.admin.core.service.project;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
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
    TupleTwo<Result<Void>, ProjectConfigPO> updateOrInitProjectConfig(ProjectConfigDTO configDTO, String operator);
    
    /**
     * 按项目id删除 通过项目id逻辑删除项目配置
     *
     * @param projectId 项目id
     */
    void deleteByProjectId(int projectId);
    
}