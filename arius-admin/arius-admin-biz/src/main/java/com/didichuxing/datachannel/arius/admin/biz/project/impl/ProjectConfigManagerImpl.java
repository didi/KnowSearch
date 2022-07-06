package com.didichuxing.datachannel.arius.admin.biz.project.impl;

import com.didichuxing.datachannel.arius.admin.biz.project.ProjectConfigManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectConfigVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author shizeying
 * @date 2022/06/06
 */
@Component
public class ProjectConfigManagerImpl implements ProjectConfigManager {
	@Autowired
	private ProjectConfigService projectConfigService;


	
	/**
	 * 获取esUserName配置信息
	 *
	 * @param projectId@return 配置信息
	 */
	@Override
	public Result<ProjectConfigVO> get(int projectId) {
		final ProjectConfigVO projectConfigVo = ConvertUtil.obj2Obj(projectConfigService.getProjectConfig(projectId),
				ProjectConfigVO.class);
		return Result.buildSucc(projectConfigVo);
	}
	


	
}