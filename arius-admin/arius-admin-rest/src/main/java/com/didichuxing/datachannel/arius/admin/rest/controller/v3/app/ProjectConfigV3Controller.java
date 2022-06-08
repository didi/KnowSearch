package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.app.ProjectConfigManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectConfigVo;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * project config 配置
 *
 * @author shizeying
 * @date 2022/05/30
 */
@RestController
@RequestMapping({ V3 + "/project-config" })
@Api(tags = "应用关联config (REST)")
public class ProjectConfigV3Controller {
	@Autowired
	private ProjectConfigManager projectConfigManager;
	
	@PutMapping("")
	@ResponseBody
	@ApiOperation(value = "修改项目配置", notes = "")
	public Result<Void> update(HttpServletRequest request, @RequestBody ProjectConfigDTO configDTO) {
		//获取操作用户
		String userName = HttpRequestUtil.getOperator(request);
		return projectConfigManager.initProjectConfig(configDTO, userName);
	}
	
	@PostMapping
	@ResponseBody
	@ApiOperation(value = "新增项目配置", notes = "")
	public Result<Void> init(HttpServletRequest request, @RequestBody ProjectConfigDTO configDTO) {
		//获取操作用户
		String userName = HttpRequestUtil.getOperator(request);
		return projectConfigManager.initProjectConfig(configDTO, userName);
	}
	
	@GetMapping
	@ResponseBody
	@ApiOperation(value = "获取项目配置", notes = "")
	public Result<ProjectConfigVo> get(HttpServletRequest request) {
		//获取操作用户慢查询耗时：ms
		Integer projectId = HttpRequestUtil.getProjectId(request);
		return projectConfigManager.get(projectId);
	}
}