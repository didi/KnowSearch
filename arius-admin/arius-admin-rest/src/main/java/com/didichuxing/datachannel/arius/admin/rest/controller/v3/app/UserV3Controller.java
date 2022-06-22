package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_SECURITY;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.app.UserExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.constant.Constants;
import com.didiglobal.logi.security.common.dto.user.UserDTO;
import com.didiglobal.logi.security.common.dto.user.UserQueryDTO;
import com.didiglobal.logi.security.common.vo.role.AssignInfoVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.common.vo.user.UserVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户v3控制器
 *
 * @author shizeying
 * @date 2022/06/14
 */
@RestController
@Api(value = "user相关API (REST)", tags = { "用户相关API接口"})
@RequestMapping(V3_SECURITY + "/user")
public class UserV3Controller {
	@Autowired
	private RoleTool          roleTool;
	@Autowired
	private UserExtendManager userManager;
	
	@GetMapping()
	@ResponseBody
	@ApiOperation(value = "获取管理员列表")
	public Result<List<UserBriefVO>> getAdminList(HttpServletRequest request) {
		return Result.buildSucc(roleTool.getAdminList());
	}
	
	@PutMapping("/add")
	@ResponseBody
	@ApiOperation(value = "用户新增接口，暂时没有考虑权限", notes = "")
	public Result<Void> add(HttpServletRequest request, @RequestBody UserDTO param) {
		return userManager.addUser(param, HttpRequestUtil.getOperator(request));
	}
	
	@GetMapping("/{type}/{value}/check")
	@ApiOperation(value = "获取用户详情", notes = "根据用户id获取用户详情")
	@ApiImplicitParam(name = "type", value = "用户id", dataType = "int", required = true)
	public Result<Void> check(@PathVariable Integer type, @PathVariable String value) {
		return userManager.check(type, value);
	}
	
	@GetMapping("/batch")
	@ApiOperation(value = "批量获取用户详情", notes = "根据用户id获取用户详情")
	@ApiImplicitParam(name = "ids", value = "用户ids", dataType = "string", example = "[1,2,3]", required = true)
	public Result<List<UserVO>> detailList(@RequestParam("ids") String ids) {
		List<Integer> idList = Lists.newArrayList();
		try {
			idList = JSON.parseArray(ids, Integer.class);
		} catch (Exception e) {
			return Result.buildParamIllegal("传入的参数不属于json数组");
		}
		
		return userManager.getUserDetailByUserIds(idList);
	}
	
	@GetMapping("/{id}")
	@ApiOperation(value = "获取用户详情", notes = "根据用户id获取用户详情")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "用户id", dataType = "int",paramType = "query",required = true)
	})
	public Result<UserVO> detail(HttpServletRequest request,@PathVariable Integer id) {
		return userManager.getUserDetailByUserId(id,HttpRequestUtil.getProjectId(request));
	}
	
	@PostMapping("/page")
	@ApiOperation(value = "查询用户列表", notes = "分页和条件查询")
	public PagingResult<UserVO> page(@RequestBody UserQueryDTO queryDTO) {
		return userManager.getUserPage(queryDTO);
	}
	
	@GetMapping("/list/dept/{deptId}")
	@ApiOperation(value = "根据部门id获取用户list", notes = "根据部门id获取用户简要信息list")
	@ApiImplicitParam(name = "deptId", value = "部门id", dataType = "int", required = true)
	public Result<List<UserBriefVO>> listByDeptId(@PathVariable Integer deptId) {
		return userManager.getUserBriefListByDeptId(deptId);
	}
	
	@GetMapping("/list/role/{roleId}")
	@ApiOperation(value = "根据角色id获取用户list", notes = "根据角色id获取用户简要信息list")
	@ApiImplicitParam(name = "roleId", value = "角色id", dataType = "int", required = true)
	public Result<List<UserBriefVO>> listByRoleId(@PathVariable Integer roleId) {
		return userManager.getUserBriefListByRoleId(roleId);
	}
	
	@GetMapping(value = "/assign/list/{userId}")
	@ApiOperation(value = "用户管理/分配角色/列表", notes = "查询所有角色列表，并根据用户id，标记该用户拥有哪些角色")
	@ApiImplicitParam(name = "userId", value = "用户id", dataType = "int", required = true)
	public Result<List<AssignInfoVO>> assignList(@PathVariable Integer userId) {
		
		return userManager.getAssignDataByUserId(userId);
	}
	
	@GetMapping(value = { "/list/{name}" })
	@ApiOperation(value = "根据账户名或用户实名查询", notes = "获取用户简要信息list，会分别以账户名和实名去模糊查询，返回两者的并集")
	@ApiImplicitParam(name = "name", value = "账户名或用户实名（为null，则获取全部用户）", dataType = "String")
	public Result<List<UserBriefVO>> listByName(@PathVariable(required = false) String name) {
		return userManager.getUserBriefListByUsernameOrRealName(name);
	}
	
	@PostMapping("/edit")
	@ResponseBody
	@ApiOperation(value = "编辑用户接口，暂时没有考虑权限", notes = "")
	public Result<Void> edit(HttpServletRequest request, @RequestBody UserDTO param) {
		return userManager.editUser(param, HttpRequestUtil.getOperator(request));
	}
	
	@DeleteMapping("/{id}")
	@ApiOperation(value = "删除用户", notes = "根据用户id删除用户")
	@ApiImplicitParam(name = "id", value = "用户id", dataType = "int", required = true)
	public Result<Void> del(@PathVariable Integer id) {
		return userManager.deleteByUserId(id);
	}
}