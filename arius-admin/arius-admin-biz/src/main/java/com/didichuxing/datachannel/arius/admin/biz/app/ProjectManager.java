package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

/**
 * 项目关联资源判断
 *
 * @author shizeying
 * @date 2022/05/25
 */
public interface ProjectManager {
	
	/**
	 * 检查项目是否绑定了逻辑集群
	 *
	 * @param projectId 项目id
	 * @return {@link Result}<{@link Void}>
	 */
	Result<Void> hasOwnLogicClusterByProject(Integer projectId);
	
	/**
	 * 检查项目是否绑定模板
	 *
	 * @param projectId 项目id
	 * @return {@link Result}<{@link Void}>
	 */
	Result<Void> hasOwnTemplateByProjectId(Integer projectId);
}