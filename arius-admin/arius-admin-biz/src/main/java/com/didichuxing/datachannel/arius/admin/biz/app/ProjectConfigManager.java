package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;

/**
 * project config
 *
 * @author shizeying
 * @date 2022/05/30
 */
public interface ProjectConfigManager {
        /**
         * 获取esUserName配置信息
         *
         * @param projectId projectId
         * @return 配置信息
         */
    Result<ProjectConfig> get(int projectId);
    
        /**
     * 更新 es user config
     *
     * @param configDTO configdto
     * @param operator 操作人 {@link AuthConstant#SUPER_USER_NAME}
     * @return {@code Result<Void>}
     */
    Result<Void> updateProjectConfig(ProjectConfigDTO configDTO, String operator);
           /**
     * 更新 es user config
     *
     * @param configDTO configdto
     * @param operator 操作人 {@link AuthConstant#SUPER_USER_NAME}
     * @return {@code Result<Void>}
     */
    Result<Void> initProjectConfig(ProjectConfigDTO configDTO, String operator);
}