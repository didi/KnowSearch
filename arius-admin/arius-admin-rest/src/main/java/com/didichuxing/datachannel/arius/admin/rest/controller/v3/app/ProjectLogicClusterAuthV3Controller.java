package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ProjectLogicClusterAuthVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * App逻辑集群权限接口
 *
 * @author wangshu
 * @date 2020/09/20
 */
@RestController
@RequestMapping({ V3 + "/project/auth/cluster" })
@Api(tags = "project逻辑集群权限接口(REST)")
public class ProjectLogicClusterAuthV3Controller {

    @Autowired
    private ProjectClusterLogicAuthService authService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @GetMapping("/app-auths")
    @ResponseBody
    @ApiOperation(value = "获取project的所有逻辑集群权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "projectId", value = "应用ID", required = true) })
    public Result<List<ProjectLogicClusterAuthVO>> getLogicClusterAuths(@RequestParam("projectId") Integer projectId) {
        return Result.buildSucc(ConvertUtil.list2List(authService.getAllLogicClusterAuths(projectId), ProjectLogicClusterAuthVO.class));
    }

    @GetMapping("/cluster-auths")
    @ResponseBody
    @ApiOperation(value = "获取project权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicClusterId", value = "逻辑集群ID", required = true) })
    public Result<List<ProjectLogicClusterAuthVO>> getAuthsByLogicClusterId(@RequestParam("logicClusterId") Long logicClusterId) {
        return Result.buildSucc(
                ConvertUtil.list2List(authService.getLogicClusterAuths(logicClusterId, null), ProjectLogicClusterAuthVO.class));
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "增加project逻辑集群权限接口" )
    public Result<Void> createLogicClusterAuth(HttpServletRequest request, @RequestBody ProjectLogicClusterAuthDTO authDTO) {
        return authService.addLogicClusterAuth(authDTO, HttpRequestUtil.getOperator(request));
    }

    @PutMapping("/{authId}")
    @ResponseBody
    @ApiOperation(value = "更新project逻辑集群权限接口" )
    public Result<Void> modifyLogicClusterAuth(HttpServletRequest request, @PathVariable(value = "authId") Long authId,
                                         @RequestBody ProjectLogicClusterAuthDTO authDTO) {
        authDTO.setId(authId);
        return authService.updateLogicClusterAuth(authDTO, HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/{authId}")
    @ResponseBody
    @ApiOperation(value = "删除APP逻辑集群权限接口")
    public Result<Void> deleteLogicClusterAuth(HttpServletRequest request,
                                               @PathVariable(value = "authId") Long authId) {

        ProjectClusterLogicAuth projectClusterLogicAuth = authService.getLogicClusterAuthById(authId);
        if (projectClusterLogicAuth == null) {
            return Result.buildNotExist("权限不存在");
        }

        List<IndexTemplate> templatesInLogicCluster = indexTemplateService
            .listHasAuthTemplatesInLogicCluster(projectClusterLogicAuth.getProjectId(), projectClusterLogicAuth.getLogicClusterId());
        if (!templatesInLogicCluster.isEmpty()) {
            return Result.buildFail("应用在集群上存在有权限的索引模板，不能删除");
        }

        return authService.deleteLogicClusterAuthById(authId, HttpRequestUtil.getOperator(request));
    }

}