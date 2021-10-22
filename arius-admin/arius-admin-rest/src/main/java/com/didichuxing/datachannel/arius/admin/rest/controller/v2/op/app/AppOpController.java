package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.AppConfigVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.AppVO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


/**
 * App Controller
 *
 * @author d06679
 * @date 2019/3/13
 */
@RestController
@RequestMapping(V2_OP + "/app")
@Api(value = "OP-APP接口(REST)")
public class AppOpController {

    @Autowired
    private AppService           appService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取APP列表接口", notes = "获取APP列表,包含APP全部元信息")
    public Result<List<AppVO>> list() {
        return Result.buildSucc(ConvertUtil.list2List(appService.getApps(), AppVO.class));
    }

    @GetMapping("/listByClusterAuth")
    @ResponseBody
    @ApiOperation(value = "获取有指定集群指定权限的APP列表", notes = "获取APP列表,包含APP全部元信息")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "query", dataType = "Long", name = "logicClusterId", required = true, value = "逻辑集群ID"),
        @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "authType", required = false, defaultValue = "2",
            value = "逻辑集群权限类型，0-超管，1-管理, 2-访问, -1-无权限"),
    })
    public Result<List<AppVO>> listByLogicClusterAuth(
        @RequestParam Long logicClusterId,
        @RequestParam(required = false, defaultValue = "1") Integer authType) {

        AppLogicClusterAuthEnum logicClusterAuth = AppLogicClusterAuthEnum.valueOf(authType);

        return Result.buildSucc(ConvertUtil.list2List(
            appService.getAppsByLowestLogicClusterAuth(logicClusterId, logicClusterAuth), AppVO.class));
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取APP接口", notes = "获取指定app,包含APP全部元信息")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result<AppVO> get(@RequestParam("appId") Integer appId) {
        return Result.buildSucc(ConvertUtil.obj2Obj(appService.getAppById(appId), AppVO.class));
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建APP接口", notes = "")
    public Result<Integer> add(HttpServletRequest request, @RequestBody AppDTO appDTO) throws Exception {
        return appService.registerApp(appDTO, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/update")
    @ResponseBody
    @ApiOperation(value = "编辑APP接口", notes = "")
    public Result update(HttpServletRequest request, @RequestBody AppDTO appDTO) {
        return appService.editApp(appDTO, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "删除APP接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result delete(HttpServletRequest request, @RequestParam("appId") Integer appId) {
        return appService.deleteAppById(appId, HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/config/get")
    @ResponseBody
    @ApiOperation(value = "获取APP配置接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result<AppConfigVO> getConfig(@RequestParam("appId") Integer appId) {
        return Result.buildSucc(ConvertUtil.obj2Obj(appService.getAppConfig(appId), AppConfigVO.class));
    }

    @PutMapping("/config/update")
    @ResponseBody
    @ApiOperation(value = "编辑APP配置接口", notes = "")
    public Result updateConfig(HttpServletRequest request, @RequestBody AppConfigDTO configDTO) {
        return appService.updateAppConfig(configDTO, HttpRequestUtils.getOperator(request));
    }
}
