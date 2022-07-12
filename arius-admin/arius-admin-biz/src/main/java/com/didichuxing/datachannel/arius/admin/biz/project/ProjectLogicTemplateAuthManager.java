package com.didichuxing.datachannel.arius.admin.biz.project;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectTemplateAuthVO;

import java.util.List;

/**
 * Created by linyunan on 2021-06-15
 */
public interface ProjectLogicTemplateAuthManager {


	/**
	 * 更新模板 更新模板权限
	 *
	 * @param authDTO  auth dto
	 * @param operator 操作符
	 * @return {@link Result}<{@link Void}>
	 */
	Result<Void> updateTemplateAuth(ProjectTemplateAuthDTO authDTO, String operator);
	
	/**
	 * 添加模板
	 *
	 * @param authDTO   auth dto
	 * @param operator  操作符
	 * @param projectId 项目id
	 * @return {@link Result}<{@link Void}>
	 */
	Result<Void> addTemplateAuth(ProjectTemplateAuthDTO authDTO, String operator, Integer projectId);
	
	/**
	 * 删除模板
	 *
	 * @param authId    身份验证id
	 * @param operator  操作符
	 * @param projectId 项目id
	 * @return {@link Result}<{@link Void}>
	 */
	Result<Void> deleteTemplateAuth(Long authId, String operator, Integer projectId);
	
	/**
	 * 删除冗余模板
	 *
	 * @param delete 删除
	 * @return {@link Result}<{@link Void}>
	 */
	Result<Void> deleteRedundancyTemplateAuths(boolean delete);
	
	/**
	 * 得到项目模板身份验证
	 *
	 * @param projectId 项目id
	 * @return {@link Result}<{@link List}<{@link ProjectTemplateAuthVO}>>
	 */
	Result<List<ProjectTemplateAuthVO>> getProjectTemplateAuths(Integer projectId);
}