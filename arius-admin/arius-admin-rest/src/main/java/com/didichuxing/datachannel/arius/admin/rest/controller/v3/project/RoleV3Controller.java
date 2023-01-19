package com.didichuxing.datachannel.arius.admin.rest.controller.v3.project;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_SECURITY;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.project.RoleExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.RoleExtendVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.knowframework.security.common.PagingResult;
import com.didiglobal.knowframework.security.common.dto.role.RoleAssignDTO;
import com.didiglobal.knowframework.security.common.dto.role.RoleQueryDTO;
import com.didiglobal.knowframework.security.common.dto.role.RoleSaveDTO;
import com.didiglobal.knowframework.security.common.vo.role.AssignInfoVO;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

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
    @ApiOperation(value = "删除角色", notes = "如果该角色是否已经分配给用户，如有分配给用户，则返回用户的信息list；否则直接删除")
    @ApiImplicitParam(name = "id", value = "角色id", dataType = "int", required = true)
    public Result delete(@PathVariable Integer id, HttpServletRequest request) {

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
    public Result<RoleExtendVO> detail(@PathVariable Integer id) {
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

    @DeleteMapping("/{id}/user/{userId}")
    @ApiOperation(value = "从角色中删除该角色下的用户", notes = "从角色中删除该角色下的用户")
    @ApiImplicitParam(name = "id", value = "角色id", dataType = "int", required = true)
    public Result<Void> deleteUser(@PathVariable Integer id, @PathVariable Integer userId, HttpServletRequest request) {

        return roleExtendManager.deleteUserFromRole(id, userId, request);
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询角色列表", notes = "分页和条件查询")
    public PagingResult<RoleExtendVO> page(@RequestBody RoleQueryDTO queryDTO) {
        return roleExtendManager.getRolePage(queryDTO);
    }

    @PostMapping("/assign")
    @ApiOperation(value = "分配角色", notes = "分配一个角色给多个用户或分配多个角色给一个用户")
    public Result<Void> assign(@RequestBody RoleAssignDTO assignDTO, HttpServletRequest request) {

        return roleExtendManager.assignRoles(assignDTO, request);

    }

    @GetMapping(value = "/assign/{roleId}")
    @ApiOperation(value = "角色管理/分配用户/列表", notes = "查询所有用户列表，并根据角色id，标记哪些用户拥有该角色")
    @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "int", required = true)
    public Result<List<AssignInfoVO>> assignList(@PathVariable Integer roleId) {
        return roleExtendManager.getAssignInfoByRoleId(roleId);

    }

}