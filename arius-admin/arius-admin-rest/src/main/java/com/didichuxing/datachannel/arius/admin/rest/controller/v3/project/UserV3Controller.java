package com.didichuxing.datachannel.arius.admin.rest.controller.v3.project;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_SECURITY;

import com.didichuxing.datachannel.arius.admin.biz.project.UserExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.UserExtendDTO;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.user.UserDTO;
import com.didiglobal.logi.security.common.dto.user.UserQueryDTO;
import com.didiglobal.logi.security.common.vo.role.AssignInfoVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.common.vo.user.UserVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Optional;
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
 * 用户v3控制器
 *
 * @author shizeying
 * @date 2022/06/14
 */
@RestController
@Api(value = "user相关API (REST)", tags = { "用户相关API接口" })
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

    @PostMapping("")
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

    @GetMapping("/{id}")
    @ApiOperation(value = "获取用户详情", notes = "根据用户id获取用户详情")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "用户id", dataType = "int", paramType = "query", required = true) })
    public Result<UserVO> detail(HttpServletRequest request, @PathVariable Integer id) {
        Integer projectId = Optional.ofNullable(HttpRequestUtil.getProjectId(request)).filter(i -> i > 0).orElse(null);

        return userManager.getUserDetailByUserId(id, projectId);
    }

    @PostMapping("/page")
    @ApiOperation(value = "查询用户列表", notes = "分页和条件查询")
    public PagingResult<UserVO> page(@RequestBody UserQueryDTO queryDTO) {
        return userManager.getUserPage(queryDTO);
    }

    @GetMapping("/dept/{deptId}")
    @ApiOperation(value = "根据部门id获取用户list", notes = "根据部门id获取用户简要信息list")
    @ApiImplicitParam(name = "deptId", value = "部门id", dataType = "int", required = true)
    public Result<List<UserBriefVO>> listByDeptId(@PathVariable Integer deptId) {
        return userManager.getUserBriefListByDeptId(deptId);
    }

    @GetMapping("/role/{roleId}")
    @ApiOperation(value = "根据角色id获取用户list", notes = "根据角色id获取用户简要信息list")
    @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "int", required = true)
    public Result<List<UserBriefVO>> listByRoleId(@PathVariable Integer roleId) {
        return userManager.getUserBriefListByRoleId(roleId);
    }

    @GetMapping(value = "/assign/{userId}")
    @ApiOperation(value = "用户管理/分配角色/列表", notes = "查询所有角色列表，并根据用户id，标记该用户拥有哪些角色")
    @ApiImplicitParam(name = "userId", value = "用户id", dataType = "int", required = true)
    public Result<List<AssignInfoVO>> assignList(@PathVariable Integer userId) {

        return userManager.getAssignDataByUserId(userId);
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "编辑用户接口，暂时没有考虑权限", notes = "")
    public Result<Void> edit(HttpServletRequest request, @RequestBody UserExtendDTO param) {
        return userManager.editUser(param, HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户", notes = "根据用户id删除用户")
    @ApiImplicitParam(name = "id", value = "用户id", dataType = "int", required = true)
    public Result<Void> del(@PathVariable Integer id) {
        return userManager.deleteByUserId(id);
    }
}