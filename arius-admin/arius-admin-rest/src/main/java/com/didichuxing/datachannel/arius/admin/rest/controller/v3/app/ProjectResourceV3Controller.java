package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.app.ProjectManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ V3 + "/project/" })
@Api(tags = "应用资源校验(REST)")
public class ProjectResourceV3Controller {
	@Autowired
	private ProjectManager projectManager;
	
	@GetMapping("logic-cluster/{projectId}")
	@ResponseBody
	@ApiOperation(value = "项目中是否绑定了逻辑集群")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "projectId", value = "projectId", required = true) })
	public Result<Void> checkProjectLogicCluster(@PathVariable("projectId") Integer projectId) {
		return projectManager.hasOwnLogicClusterByProject(projectId);
	}
	
	@GetMapping("template/{projectId}")
	@ResponseBody
	@ApiOperation(value = "项目中是否绑定了模板")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "projectId", value = "projectId", required = true) })
	public Result<Void> checkProjectTemplate(@PathVariable("projectId") Integer projectId) {
		return projectManager.hasOwnTemplateByProjectId(projectId);
	}
	
}