package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_SECURITY;

import com.didichuxing.datachannel.arius.admin.biz.app.RoleExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.constant.Constants;
import com.didiglobal.logi.security.common.dto.role.RoleAssignDTO;
import com.didiglobal.logi.security.common.dto.role.RoleQueryDTO;
import com.didiglobal.logi.security.common.dto.role.RoleSaveDTO;
import com.didiglobal.logi.security.common.vo.role.AssignInfoVO;
import com.didiglobal.logi.security.common.vo.role.RoleBriefVO;
import com.didiglobal.logi.security.common.vo.role.RoleDeleteCheckVO;
import com.didiglobal.logi.security.common.vo.role.RoleVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色v3控制器
 *
 * @author shizeying
 * @date 2022/06/08
 */
@RestController
@RequestMapping({ V3_SECURITY + "/role" })
@Api(value = "role相关API接口", tags = { "角色相关API接口" })
public class RoleV3Controller {
	@Autowired
	private RoleTool          roleTool;
	@Autowired
	private RoleExtendManager roleExtendManager;
	
	@GetMapping("/is-admin")
	@ResponseBody
	@ApiOperation(value = "判断是否为管理员")
	public Result<Void> isAdmin(HttpServletRequest request) {
		final String operator = HttpRequestUtil.getOperator(request);
		if (!roleTool.isAdmin(operator)) {
			return Result.buildFail("当前角色非管理员");
		}
		return Result.buildSucc();
	}
	
	@DeleteMapping("/{id}")
	@ApiOperation(value = "删除角色", notes = "根据角色id删除角色")
	@ApiImplicitParam(name = "id", value = "角色id", dataType = "int", required = true)
	public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
		
		return roleExtendManager.deleteRoleByRoleId(id, request);
	}
	
	@GetMapping("/admin")
	@ResponseBody
	@ApiOperation(value = "返回管理员列表id")
	public Result<List<Integer>> getAdminRoleIds(HttpServletRequest request) {
		return Result.buildSucc(Lists.newArrayList(AuthConstant.ADMIN_ROLE_ID));
	}
	
	@GetMapping("/resource-owner")
	@ResponseBody
	@ApiOperation(value = "资源owner角色id")
	public Result<List<Integer>> getResourceOwnerId(HttpServletRequest request) {
		return Result.buildSucc(Lists.newArrayList(AuthConstant.ADMIN_ROLE_ID));
	}
	
	@GetMapping("/{id}")
	@ApiOperation(value = "获取角色详情", notes = "根据角色id或角色code获取角色详情")
	@ApiImplicitParam(name = "id", value = "角色id", dataType = "int", required = true)
	public Result<RoleVO> detail(@PathVariable Integer id) {
		return roleExtendManager.getRoleDetailByRoleId(id);
	}
	
	@PutMapping
	@ApiOperation(value = "更新角色信息", notes = "根据角色id更新角色信息")
	public Result<Void> update(@RequestBody RoleSaveDTO saveDTO, HttpServletRequest request) {
		return roleExtendManager.updateRole(saveDTO, request);
	}
	
	@PostMapping
	@ApiOperation(value = "创建角色", notes = "创建角色")
	public Result<Void> create(@RequestBody RoleSaveDTO saveDTO, HttpServletRequest request) {
		return roleExtendManager.createRole(saveDTO, request);
	}
	
	@DeleteMapping("/delete/check/{id}")
	@ApiOperation(value = "删除角色前的检查", notes = "判断该角色是否已经分配给用户，如有分配给用户，则返回用户的信息list")
	@ApiImplicitParam(name = "id", value = "角色id", dataType = "int", required = true)
	public Result<RoleDeleteCheckVO> check(@PathVariable Integer id) {
		return roleExtendManager.checkBeforeDelete(id);
	}
	
	@DeleteMapping("/{id}/user/{userId}")
	@ApiOperation(value = "从角色中删除该角色下的用户", notes = "从角色中删除该角色下的用户")
	@ApiImplicitParam(name = "id", value = "角色id", dataType = "int", required = true)
	public Result<Void> deleteUser(@PathVariable Integer id, @PathVariable Integer userId, HttpServletRequest request) {
		
		return roleExtendManager.deleteUserFromRole(id, userId, request);
	}
	
	@PostMapping("/page")
	@ApiOperation(value = "分页查询角色列表", notes = "分页和条件查询")
	public PagingResult<RoleVO> page(@RequestBody RoleQueryDTO queryDTO) {
		return roleExtendManager.getRolePage(queryDTO);
	}
	
	@PostMapping("/assign")
	@ApiOperation(value = "分配角色", notes = "分配一个角色给多个用户或分配多个角色给一个用户")
	public Result<Void> assign(@RequestBody RoleAssignDTO assignDTO, HttpServletRequest request) {
		
		return roleExtendManager.assignRoles(assignDTO, request);
		
	}
	
	@GetMapping(value = "/assign/list/{roleId}")
	@ApiOperation(value = "角色管理/分配用户/列表", notes = "查询所有用户列表，并根据角色id，标记哪些用户拥有该角色")
	@ApiImplicitParam(name = "roleId", value = "角色id", dataType = "int", required = true)
	public Result<List<AssignInfoVO>> assignList(@PathVariable Integer roleId) {
		return roleExtendManager.getAssignInfoByRoleId(roleId);
		
	}
	
	@GetMapping(value = { "/list/{roleName}", "/list" })
	@ApiOperation(value = "根据角色名模糊查询", notes = "用户管理/列表查询条件/分配角色框，这里会用到此接口")
	@ApiImplicitParam(name = "roleName", value = "角色名（为null，查询全部）", dataType = "String")
	public Result<List<RoleBriefVO>> list(@PathVariable(required = false) String roleName) {
		return roleExtendManager.getRoleBriefListByRoleName(roleName);
	}
	
}