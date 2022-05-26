package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.app.ESUserManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目关联的es user
 *
 * @author shizeying
 * @date 2022/05/26
 * @since 0.3
 */
@RestController
@RequestMapping({ V3 + "/project/es-user/" })
@Api(tags = "应用关联es user (REST)")
public class ProjectESUserV3Controller {
	
	@Autowired
	private ESUserManager esUserManager;
    
    @PutMapping("{projectId}")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "projectId", value = "projectId", required = true) })
    public Result<Integer> createESUerByProject(HttpServletRequest request,
                                                @PathVariable("projectId") Integer projectId,
                                                @RequestBody ESUserDTO appDTO) {
        return esUserManager.registerESUser(appDTO, projectId, HttpRequestUtil.getOperator(request));
    }
	
	
	@GetMapping("all")
	@ResponseBody
	@ApiOperation(value = "管理员获取所有项目的es user")
	public Result<List<ESUser>> listESUserByALLProject(HttpServletRequest request) {
		final String operator = HttpRequestUtil.getOperator(request);
		if (!operator.equals(AdminConstant.SUPER_USER_NAME)) {
			return Result.buildFail("当前用户权限不足");
		}
		return esUserManager.listESUsersByAllProject();
	}
	
	@GetMapping("")
	@ResponseBody
	@ApiOperation(value = "获取项目下所有的es user")
	public Result<List<ESUser>> listESUserByProjectId(HttpServletRequest request) {
		return esUserManager.listESUsersByProjectId(HttpRequestUtil.getProjectId(request),
				HttpRequestUtils.getOperator(request));
	}
	
	@DeleteMapping("{esUser}")
	@ResponseBody
	@ApiOperation(value = "删除项目下指定的es user")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "esUser", value = "es user", required = true) })
	public Result<Void> deleteESUserByProject(HttpServletRequest request, @PathVariable("esUser") Integer esUserName) {
		return esUserManager.deleteESUserByProject(esUserName, HttpRequestUtil.getProjectId(request),
				HttpRequestUtils.getOperator(request));
	}
	
	@DeleteMapping("")
	@ResponseBody
	@ApiOperation(value = "删除项目下全部的es user")
	public Result<Void> deleteAllESUserByProject(HttpServletRequest request) {
		return esUserManager.deleteAllESUserByProject(HttpRequestUtil.getProjectId(request),
				HttpRequestUtils.getOperator(request));
	}
	
	@PutMapping("/update")
	@ResponseBody
	@ApiOperation(value = "编辑APP接口", notes = "支持修改数据中心、备注")
	public Result<Void> update(HttpServletRequest request, @RequestBody ConsoleESUserDTO appDTO) {
		return esUserManager.update(request, appDTO);
	}
	

	 @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取es user详情接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "esUser", value = "esUser详情", required = true) })
    public Result<ConsoleESUserVO> get(@RequestParam("esUser") Integer esUser) {
        return esUserManager.get(esUser);
    }
	
	@GetMapping("/list")
	@ResponseBody
	@ApiOperation(value = "es user 列表接口")
	public Result<List<ConsoleESUserVO>> list() {
		return esUserManager.list();
	}

}