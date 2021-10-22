package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

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

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicClusterAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;

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
@RequestMapping({ V2_OP + "/app/auth", V3_OP + "/app/auth/cluster" })
@Api(value = "OP-App逻辑集群权限接口(REST)")
public class AppLogicClusterAuthController {

    @Autowired
    private AppLogicClusterAuthService authService;

    @Autowired
    private TemplateLogicService       templateLogicService;

    @GetMapping("/appAuths")
    @ResponseBody
    @ApiOperation(value = "获取APP的所有逻辑集群权限接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result<List<AppLogicClusterAuthDTO>> getLogicClusterAuths(@RequestParam("appId") Integer appId) {
        return Result.buildSucc(authService.getLogicClusterAuths(appId));
    }

    @GetMapping("/clusterAuths")
    @ResponseBody
    @ApiOperation(value = "获取APP权限接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicClusterId", value = "逻辑集群ID", required = true) })
    public Result<List<AppLogicClusterAuthDTO>> getClusterAuths(@RequestParam("logicClusterId") Long logicClusterId) {
        return Result.buildSucc(authService.getLogicClusterAuths(logicClusterId, null));
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "增加APP逻辑集群权限接口", notes = "")
    public Result createLogicClusterAuth(HttpServletRequest request, @RequestBody AppLogicClusterAuthDTO authDTO) {
        return authService.addLogicClusterAuth(authDTO, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/{authId}")
    @ResponseBody
    @ApiOperation(value = "更新APP逻辑集群权限接口", notes = "")
    public Result modifyLogicClusterAuth(HttpServletRequest request, @PathVariable(value = "authId") Long authId,
                                         @RequestBody AppLogicClusterAuthDTO authDTO) {
        authDTO.setId(authId);
        return authService.updateLogicClusterAuth(authDTO, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/{authId}")
    @ResponseBody
    @ApiOperation(value = "删除APP逻辑集群权限接口", notes = "")
    public Result deleteLogicClusterAuth(HttpServletRequest request, @PathVariable(value = "authId") Long authId) {

        AppLogicClusterAuthDTO logicClusterAuthDTO = authService.getLogicClusterAuthById(authId);
        if (logicClusterAuthDTO == null) {
            return Result.buildNotExist("权限不存在");
        }

        if (logicClusterAuthDTO.getLogicClusterId() != null) {
            List<IndexTemplateLogic> templatesInLogicCluster = templateLogicService.getHasAuthTemplatesInLogicCluster(
                logicClusterAuthDTO.getAppId(), logicClusterAuthDTO.getLogicClusterId());
            if (templatesInLogicCluster.size() > 0) {
                return Result.buildFail("应用在集群上存在有权限的索引，不能删除");
            }
        }

        return authService.deleteLogicClusterAuthById(authId, HttpRequestUtils.getOperator(request));
    }

}
