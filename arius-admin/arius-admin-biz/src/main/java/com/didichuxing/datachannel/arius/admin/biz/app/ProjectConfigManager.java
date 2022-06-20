package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectConfigVO;

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
    Result<ProjectConfigVO> get(int projectId);
    


   
}