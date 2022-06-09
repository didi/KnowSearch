package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.app.ProjectExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectExtendSaveDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectBriefExtendVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectExtendVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目v3控制器
 *
 * @author shizeying
 * @date 2022/06/01
 */
@RestController
@RequestMapping({ V3 + "/project" })
@Api(tags = "应用获取 (REST)")
public class ProjectV3Controller {
	@Autowired
	private RoleTool             roleTool;
	@Autowired
	private ProjectExtendManager projectExtendManager;
	
	@GetMapping("/admin-project")
	@ResponseBody
	@ApiOperation(value = "获取超级应用id")
	public Result<Integer> isAdminProject(HttpServletRequest request) {
		final String operator = HttpRequestUtil.getOperator(request);
		if (!roleTool.isAdmin(operator)) {
			return Result.buildFail("当前角色非管理员");
		}
		return Result.buildSucc(AuthConstant.SUPER_PROJECT_ID);
	}
	
	@GetMapping("/{id}")
	@ResponseBody
	@ApiOperation(value = "获取项目详情", notes = "根据项目id获取项目详情")
	@ApiImplicitParam(name = "id", value = "项目id", dataType = "int", required = true)
	public com.didiglobal.logi.security.common.Result<ProjectExtendVO> detail(@PathVariable Integer id) {
		return projectExtendManager.getProjectDetailByProjectId(id);
	}
	
	@PostMapping
	@ResponseBody
	@ApiOperation(value = "创建项目", notes = "创建项目")
	public com.didiglobal.logi.security.common.Result<ProjectExtendVO> create(@RequestBody ProjectExtendSaveDTO saveDTO,
	                                                                          HttpServletRequest request) {
		return projectExtendManager.createProject(saveDTO, HttpRequestUtil.getOperator(request));
	}
	
	@DeleteMapping("/{id}")
	@ResponseBody
	@ApiOperation(value = "删除项目", notes = "根据项目id删除项目")
	@ApiImplicitParam(name = "id", value = "项目id", dataType = "int", required = true)
	public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
		
		return projectExtendManager.deleteProjectByProjectId(id, HttpRequestUtil.getOperator(request));
	}
	
	@GetMapping("/list")
	@ResponseBody
	@ApiOperation(value = "获取所有项目简要信息", notes = "获取全部项目简要信息（只返回id、项目名）")
	public com.didiglobal.logi.security.common.Result<List<ProjectBriefExtendVO>> list() {
		return projectExtendManager.getProjectBriefList();
	}
	
}