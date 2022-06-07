package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.APP_CONFIG;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;

import com.didichuxing.datachannel.arius.admin.biz.app.ProjectConfigManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectConfigVo;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
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

	@Autowired
	private OperateRecordService operateRecordService;
	
	/**
	 * 获取esUserName配置信息
	 *
	 * @param projectId@return 配置信息
	 */
	@Override
	public Result<ProjectConfigVo> get(int projectId) {
		final ProjectConfigVo projectConfigVo = ConvertUtil.obj2Obj(projectConfigService.getProjectConfig(projectId),
				ProjectConfigVo.class);
		return Result.buildSucc(projectConfigVo);
	}
	
	/**
	 * 更新 es user config
	 *
	 * @param configDTO configdto
	 * @param operator  操作人或角色
	 * @return {@code Result<Void>}
	 */
	@Override
	public Result<Void> updateProjectConfig(ProjectConfigDTO configDTO, String operator) {
		//只有success时候会存在tuple._2不为null
		final Tuple<Result<Void>, ProjectConfigPO> tuple = projectConfigService.updateOrInitProjectConfig(configDTO,
				operator);
		if (tuple.getV1().success()) {
			operateRecordService.save(APP_CONFIG, EDIT, configDTO.getProjectId(),
					AriusObjUtils.findChangedWithClear(tuple.getV2(), configDTO), operator);
		}
		return tuple.getV1();
	}
	
	@Override
	public Result<Void> initProjectConfig(ProjectConfigDTO configDTO, String operator) {
			//只有success时候会存在tuple._2不为null
		final Tuple<Result<Void>, ProjectConfigPO> tuple = projectConfigService.updateOrInitProjectConfig(configDTO,
				operator);
		if (tuple.getV1().success()) {
			operateRecordService.save(APP_CONFIG, ADD, configDTO.getProjectId(),
					AriusObjUtils.findChangedWithClear(tuple.getV2(), configDTO), operator);
		}
		return tuple.getV1();
	}
	
	/**
	 * @param projectId
	 */
	@Override
	public void deleteByProjectId(Integer projectId) {
		projectConfigService.deleteByProjectId(projectId);
		
	}
	
}