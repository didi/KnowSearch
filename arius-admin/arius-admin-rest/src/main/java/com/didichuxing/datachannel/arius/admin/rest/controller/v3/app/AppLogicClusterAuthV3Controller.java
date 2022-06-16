package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.AppLogicClusterAuthVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;

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
@RequestMapping({ V3 + "/app/auth/cluster" })
@Api(tags = "App逻辑集群权限接口(REST)")
public class AppLogicClusterAuthV3Controller {

    @Autowired
    private AppClusterLogicAuthService authService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @GetMapping("/app-auths")
    @ResponseBody
    @ApiOperation(value = "获取APP的所有逻辑集群权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result<List<AppLogicClusterAuthVO>> getLogicClusterAuths(@RequestParam("appId") Integer appId) {
        return Result.buildSucc(ConvertUtil.list2List(authService.getAllLogicClusterAuths(appId), AppLogicClusterAuthVO.class));
    }

    @GetMapping("/cluster-auths")
    @ResponseBody
    @ApiOperation(value = "获取APP权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicClusterId", value = "逻辑集群ID", required = true) })
    public Result<List<AppLogicClusterAuthVO>> getAuthsByLogicClusterId(@RequestParam("logicClusterId") Long logicClusterId) {
        return Result.buildSucc(
                ConvertUtil.list2List(authService.getLogicClusterAuths(logicClusterId, null), AppLogicClusterAuthVO.class));
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "增加APP逻辑集群权限接口" )
    public Result<Void> createLogicClusterAuth(HttpServletRequest request, @RequestBody AppLogicClusterAuthDTO authDTO) {
        return authService.addLogicClusterAuth(authDTO, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/{authId}")
    @ResponseBody
    @ApiOperation(value = "更新APP逻辑集群权限接口" )
    public Result<Void> modifyLogicClusterAuth(HttpServletRequest request, @PathVariable(value = "authId") Long authId,
                                         @RequestBody AppLogicClusterAuthDTO authDTO) {
        authDTO.setId(authId);
        return authService.updateLogicClusterAuth(authDTO, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/{authId}")
    @ResponseBody
    @ApiOperation(value = "删除APP逻辑集群权限接口")
    public Result<Void> deleteLogicClusterAuth(HttpServletRequest request,
                                               @PathVariable(value = "authId") Long authId) {

        AppClusterLogicAuth appClusterLogicAuth = authService.getLogicClusterAuthById(authId);
        if (appClusterLogicAuth == null) {
            return Result.buildNotExist("权限不存在");
        }

        List<IndexTemplate> templatesInLogicCluster = indexTemplateService
            .listHasAuthTemplatesInLogicCluster(appClusterLogicAuth.getAppId(), appClusterLogicAuth.getLogicClusterId());
        if (!templatesInLogicCluster.isEmpty()) {
            return Result.buildFail("应用在集群上存在有权限的索引模板，不能删除");
        }

        return authService.deleteLogicClusterAuthById(authId, HttpRequestUtils.getOperator(request));
    }

}
