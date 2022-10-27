package com.didichuxing.datachannel.arius.admin.rest.controller.v3.project;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_SECURITY;

import com.didichuxing.datachannel.arius.admin.biz.project.ProjectConfigManager;
import com.didichuxing.datachannel.arius.admin.biz.project.ProjectExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectExtendSaveDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectQueryExtendDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectBriefExtendVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectExtendVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.vo.project.ProjectDeleteCheckVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Objects;
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
 * 项目v3控制器
 *
 * @author shizeying
 * @date 2022/06/01
 */
@RestController
@RequestMapping({ V3_SECURITY + "/project" })
@Api(value = "应用 (REST)", tags = { "项目相关API接口" })
public class ProjectV3Controller {
    @Autowired
    private RoleTool             roleTool;
    @Autowired
    private ProjectExtendManager projectExtendManager;
    @Autowired
    private ProjectConfigManager projectConfigManager;

    @GetMapping("/config/{projectId}")
    @ResponseBody
    @ApiOperation(value = "获取项目配置", notes = "")
    @ApiImplicitParam(name = "projectId", value = "项目id", dataType = "int", required = true)
    public Result<ProjectConfigVO> get(HttpServletRequest request, @PathVariable("projectId") Integer projectId) {
        //获取操作用户慢查询耗时：ms
        Integer id = Objects.isNull(projectId) ? HttpRequestUtil.getProjectId(request) : projectId;
        return projectConfigManager.get(id);
    }

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
    @ApiImplicitParam(name = "id", value = "项目id", dataType = "int", paramType = "path", required = true)
    public Result<ProjectExtendVO> detail(@PathVariable Integer id) {
        return projectExtendManager.getProjectDetailByProjectId(id);
    }

    @PostMapping
    @ResponseBody
    @ApiOperation(value = "创建项目", notes = "创建项目")
    public Result<ProjectExtendVO> create(@RequestBody ProjectExtendSaveDTO saveDTO, HttpServletRequest request) {

        return projectExtendManager.createProject(saveDTO, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getOperatorId(request));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "删除项目", notes = "根据项目id删除项目")
    @ApiImplicitParam(name = "id", value = "项目id", dataType = "int", paramType = "path", required = true)
    public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {

        return projectExtendManager.deleteProjectByProjectId(id, HttpRequestUtil.getOperator(request));
    }
    
    @GetMapping("/check-resources/{id}")
    @ResponseBody
    @ApiOperation(value = "检查一个项目的资源是否可用", notes = "检查一个项目的资源是否可用")
    @ApiImplicitParam(name = "id", value = "项目 id", dataType = "int", paramType = "path", required = true)
    public Result<Void> checkResourcesByProjectId(@PathVariable Integer id, HttpServletRequest request) {
        
        return projectExtendManager.checkResourcesByProjectId(id);
    }
    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取所有项目简要信息", notes = "获取全部项目简要信息（只返回id、项目名）")
    public Result<List<ProjectBriefExtendVO>> list(HttpServletRequest request) {
        return projectExtendManager.getProjectBriefList();
    }

    @GetMapping("/{id}/exist")
    @ApiOperation(value = "校验项目是否存在", notes = "校验项目是否存在")
    @ApiImplicitParam(name = "id", value = "项目id", dataType = "int", paramType = "path", required = true)
    public Result<Void> checkExist(@PathVariable Integer id) {
        return projectExtendManager.checkProjectExist(id);
    }

    @PutMapping("/switch/{id}")
    @ApiOperation(value = "更改项目运行状态", notes = "调用该接口则项目运行状态被反转")
    @ApiImplicitParam(name = "id", value = "项目id", dataType = "int", paramType = "path", required = true)
    public Result<Void> switched(@PathVariable Integer id, HttpServletRequest request) {

        return projectExtendManager.changeProjectStatus(id, HttpRequestUtil.getOperator(request));
    }

    @PutMapping
    @ApiOperation(value = "更新项目", notes = "根据项目id更新项目信息")
    public Result<Void> update(@RequestBody ProjectExtendSaveDTO saveDTO, HttpServletRequest request) {

        return projectExtendManager.updateProject(saveDTO, HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/delete/check/{id}")
    @ApiOperation(value = "删除项目前的检查", notes = "检查是否有服务引用了该项目、是否有具体资源挂上了该项目")
    @ApiImplicitParam(name = "id", value = "项目id", dataType = "int", paramType = "path", required = true)
    public Result<ProjectDeleteCheckVO> deleteCheck(@PathVariable Integer id) {
        return projectExtendManager.checkBeforeDelete(id);
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询项目列表", notes = "分页和条件查询")
    public PagingResult<ProjectExtendVO> page(@RequestBody ProjectQueryExtendDTO queryDTO, HttpServletRequest request) {
        return projectExtendManager.getProjectPage(queryDTO, request);
    }

    @PutMapping("/{id}/owner")
    @ApiOperation(value = "从角色中增加该项目下的负责人", notes = "从角色中增加该项目下的用户")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "项目id", dataType = "int", paramType = "path", required = true),
                         @ApiImplicitParam(name = "ownerList", value = "用户id列表", allowMultiple = true, dataType = "int", paramType = "body", required = true) })
    public Result<Void> addProjectOwner(@PathVariable("id") Integer id, @RequestBody List<Integer> ownerList,
                                        HttpServletRequest request) {

        return projectExtendManager.addProjectOwner(id, ownerList, HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/{id}/owner/{ownerId}")
    @ApiOperation(value = "从项目中删除该项目下的负责人", notes = "从项目中删除该项目下的负责人")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "项目id", dataType = "int", paramType = "path", required = true),
                         @ApiImplicitParam(name = "ownerId", value = "项目id", dataType = "int", paramType = "path", required = true) })
    public Result<Void> deleteProjectOwner(@PathVariable Integer id, @PathVariable Integer ownerId,
                                           HttpServletRequest request) {
        return projectExtendManager.delProjectOwner(id, ownerId, HttpRequestUtil.getOperator(request));
    }

    @PutMapping("/{id}/user")
    @ApiOperation(value = "从角色中增加该项目下的用户", notes = "从角色中增加该项目下的用户")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "项目id", dataType = "int", paramType = "path", required = true),
                         @ApiImplicitParam(name = "userList", value = "用户id列表", allowMultiple = true, dataType = "int", paramType = "body", required = true) })
    public Result<Void> addProjectUser(@PathVariable Integer id, @RequestBody List<Integer> userList,
                                       HttpServletRequest request) {

        return projectExtendManager.addProjectUser(id, userList, HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/{id}/user/{userId}")
    @ApiOperation(value = "从项目中删除该项目下的用户", notes = "从项目中删除该项目下的用户")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "项目id", dataType = "int", required = true, paramType = "path"),
                         @ApiImplicitParam(name = "userId", value = "用户id", dataType = "int", required = true, paramType = "path") })
    public Result<Void> deleteProjectUser(@PathVariable Integer id, @PathVariable Integer userId,
                                          HttpServletRequest request) {

        return projectExtendManager.delProjectUser(id, userId, HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/unassigned")
    @ApiOperation(value = "获取项目未分配的用户列表", notes = "获取项目未分配的用户列表")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "项目 id", dataType = "int", required = true),
                         @ApiImplicitParam(name = "containsAdminRole", value = "是否包含管理员", dataType = "Boolean", required =
                                 true)
    
    })
    public Result<List<UserBriefVO>> unassigned(@RequestParam("id") Integer id,
                                                @RequestParam(value = "containsAdminRole",required = false,defaultValue = "false") Boolean containsAdminRole) {

        return projectExtendManager.unassignedByProjectId(id,containsAdminRole);
    }

    @GetMapping("/user/{userId}")
    @ApiOperation(value = "获取用户绑定的项目列表", notes = "获取用户绑定的项目列表")
    @ApiImplicitParam(name = "userId", value = "用户id", dataType = "int", required = true, paramType = "path")
    public Result<List<ProjectBriefExtendVO>> getProjectBriefByUserId(@PathVariable("userId") Integer userId) {
        if (roleTool.isAdmin(userId)) {
            return projectExtendManager.getProjectBriefList();
        }
        return projectExtendManager.getProjectBriefByUserId(userId);
    }

    @GetMapping("/bind-user")
    @ApiOperation(value = "获取项目绑定的用户列表", notes = "获取项目绑定的用户列表")
    public Result<List<UserBriefVO>> getProjectBriefByUserId(HttpServletRequest request) {

        return projectExtendManager.listUserListByProjectId(HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/check-cluster-bind-gateway")
    @ResponseBody
    @ApiOperation(value = "获取当前操作项目下的集群是否绑定gateway", tags = "")
    public Result<Boolean> projectExistenceGatewayCluster(HttpServletRequest request) {
        
        return Result.buildSucc(Boolean.TRUE);
    }

}