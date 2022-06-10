package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectExtendSaveDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectBriefExtendVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectExtendVO;
import com.didiglobal.logi.security.common.Result;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import java.util.List;

/**
 * 基于logi的扩展能力
 *
 * @author shizeying
 * @date 2022/06/10
 */
public interface ProjectExtendManager {
	    /**
         * 创建项目
         *
         * @param saveDTO  项目信息
         * @param operator 请求信息
         * @return 项目信息
         * @throws LogiSecurityException 项目相关的错误信息
         */
    Result<ProjectExtendVO> createProject(ProjectExtendSaveDTO saveDTO, String operator);

    /**
     * 获取项目详情，通过项目id
     *
     * @param projectId 项目id
     * @return ProjectVo 项目信息
     * @throws LogiSecurityException 项目不存在
     */
     Result<ProjectExtendVO> getProjectDetailByProjectId(Integer projectId) ;


     
 
    /**
     * 获取所有项目简要信息
     *
     * @return 项目简要信息list
     */
    Result<List<ProjectBriefExtendVO>> getProjectBriefList();

  

    

        /**
         * 删除项目
         *
         * @param projectId 项目id
         * @param operator  请求信息
         * @return
         */
    com.didichuxing.datachannel.arius.admin.common.bean.common.Result<Void> deleteProjectByProjectId(Integer projectId, String operator);
    
}