package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.AppVO;
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
@Api(tags = "OP-运维侧APP接口(REST)")
public class AppOpController {

    @Autowired
    private AppService           appService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取APP列表接口", notes = "获取APP列表,包含APP全部元信息")
    public Result<List<AppVO>> list() {
        return Result.buildSucc(ConvertUtil.list2List(appService.listApps(), AppVO.class));
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
    @ApiOperation(value = "新建APP接口" )
    public Result<Integer> add(HttpServletRequest request, @RequestBody AppDTO appDTO) {
        return appService.registerApp(appDTO, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "删除APP接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result<Void> delete(HttpServletRequest request, @RequestParam("appId") Integer appId) {
        return appService.deleteAppById(appId, HttpRequestUtils.getOperator(request));
    }
}
