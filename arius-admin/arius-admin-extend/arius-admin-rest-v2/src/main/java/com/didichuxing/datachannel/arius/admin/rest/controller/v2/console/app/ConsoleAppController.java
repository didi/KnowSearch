package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.app.AppManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleAppDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleAppLoginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppWithVerifyCodeVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @author linyunan
 * @date 2021-04-28
 */
@RestController
@RequestMapping(V2_CONSOLE + "/app")
@Api(tags = "Console-用户侧APP相关接口(REST)")
@Deprecated
public class ConsoleAppController {

    @Autowired
    private AppManager appManager;

    @PutMapping("/login")
    @ResponseBody
    @ApiOperation(value = "登陆APP接口" )
    public Result<Void> login(HttpServletRequest request, @RequestBody ConsoleAppLoginDTO loginDTO) {
        return appManager.login(request, loginDTO);
    }

    @GetMapping("/getNoCodeLogin")
    @ResponseBody
    @ApiOperation(value = "查询用户可以免密登陆的APP接口", notes = "该接口包含APP的校验码等敏感信息,需要调用方提供ticket")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-APP-TICKET", value = "接口ticket", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "String", name = "user", value = "用户名", required = true) })
    public Result<List<ConsoleAppWithVerifyCodeVO>> getNoCodeLogin(HttpServletRequest request,
                                                                   @RequestParam("user") String user) {
        return appManager.getNoCodeLogin(request, user);
    }

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "APP列表接口" )
    public Result<List<ConsoleAppVO>> list() {
        return appManager.list();
    }

    @PutMapping("/update")
    @ResponseBody
    @ApiOperation(value = "编辑APP接口", notes = "支持修改责任人、部门信息、备注")
    public Result<Void> update(HttpServletRequest request, @RequestBody ConsoleAppDTO appDTO) {
        return appManager.update(request, appDTO);
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取APP详情接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "projectId", value = "应用ID", required = true) })
    public Result<ConsoleAppVO> get(@RequestParam("projectId") Integer projectId) {
        return appManager.get(projectId);
    }

}