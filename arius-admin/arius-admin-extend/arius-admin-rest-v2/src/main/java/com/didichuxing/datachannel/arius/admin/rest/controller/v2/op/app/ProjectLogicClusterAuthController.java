package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectLogicClusterAuthVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * App逻辑集群权限接口
 *
 * @author wangshu
 * @date 2020/09/20
 */
@RestController
@RequestMapping({ V2_OP + "/app/auth", V3_OP + "/app/auth/cluster" })
@Api(tags = "OP-运维侧App逻辑集群权限接口(REST)")
@Deprecated
public class ProjectLogicClusterAuthController {

    @Autowired
    private ProjectClusterLogicAuthService authService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @GetMapping("/appAuths")
    @ResponseBody
    @ApiOperation(value = "获取project的所有逻辑集群权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "projectId", value = "应用ID", required = true) })
    public Result<List<ProjectLogicClusterAuthVO>> getLogicClusterAuths(@RequestParam("projectId") Integer projectId) {
        return Result.buildSucc(ConvertUtil.list2List(authService.getAllLogicClusterAuths(projectId), ProjectLogicClusterAuthVO.class));
    }

    @GetMapping("/clusterAuths")
    @ResponseBody
    @ApiOperation(value = "获取APP权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicClusterId", value = "逻辑集群ID", required = true) })
    public Result<List<ProjectLogicClusterAuthVO>> getClusterAuths(@RequestParam("logicClusterId") Long logicClusterId) {
        return Result.buildSucc(
            ConvertUtil.list2List(authService.getLogicClusterAuths(logicClusterId, null), ProjectLogicClusterAuthVO.class));
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "增加APP逻辑集群权限接口" )
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
    @ApiOperation(value = "删除project逻辑集群权限接口" )
    public Result<Void> deleteLogicClusterAuth(HttpServletRequest request, @PathVariable(value = "authId") Long authId) {

        ProjectClusterLogicAuth projectClusterLogicAuth = authService.getLogicClusterAuthById(authId);
        if (projectClusterLogicAuth == null) {
            return Result.buildNotExist("权限不存在");
        }

        if (projectClusterLogicAuth.getLogicClusterId() != null) {
            List<IndexTemplate> templatesInLogicCluster = indexTemplateService.listHasAuthTemplatesInLogicCluster(
                    projectClusterLogicAuth.getProjectId(), projectClusterLogicAuth.getLogicClusterId());
            if (!templatesInLogicCluster.isEmpty()) {
                return Result.buildFail("应用在集群上存在有权限的索引模板，不能删除");
            }
        }

        return authService.deleteLogicClusterAuthById(authId, HttpRequestUtil.getOperator(request));
    }

}