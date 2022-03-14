package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplateLogicAliasesManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.ConsoleLogicTemplateAliasesDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.ConsoleLogicTemplateDeleteAliasesDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.ConsoleTemplateAliasSwitchDTO;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyAlias;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicAliasService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;

@RestController
@RequestMapping(V2_CONSOLE + "/template/aliases")
@Api(tags = "Console-用户侧索引别名接口(REST)")
public class ConsoleTemplateAliaseController extends BaseConsoleTemplateController{

    @Autowired
    private TemplateLogicAliasesManager templateLogicAliasesManager;
    @Autowired
    private TemplateLogicAliasService templateLogicAliasService;

    @Autowired
    private AppService                  appService;

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取索引Aliases接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<List<IndexTemplatePhyAlias>> getTemplateAliases(@RequestParam("logicId") Integer logicId) {
        return templateLogicAliasesManager.getAliases(logicId);
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "新增索引别名列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-APP-ID", value = "应用ID", required = true) })
    public Result<Void> createTemplateAlias(HttpServletRequest request,
                                      @RequestBody ConsoleLogicTemplateAliasesDTO aliases) {
        Result<Void> checkAuthResult = checkAppAuth(aliases.getLogicId(), HttpRequestUtils.getAppId(request));
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }

        return templateLogicAliasesManager.createAliases(aliases, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "更新索引模板别名", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-APP-ID", value = "应用ID", required = true) })
    public Result<Void> modifyTemplateAliases(HttpServletRequest request,
                                        @RequestBody ConsoleLogicTemplateAliasesDTO aliases) {
        Result<Void> checkAuthResult = checkAppAuth(aliases.getLogicId(), HttpRequestUtils.getAppId(request));
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }

        return templateLogicAliasesManager.modifyAliases(aliases, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "删除模板别名", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-APP-ID", value = "应用ID", required = true) })
    public Result<Void> deleteTemplateAliases(HttpServletRequest request,
                                        @RequestBody ConsoleLogicTemplateDeleteAliasesDTO aliases) {
        Result<Void> checkAuthResult = checkAppAuth(aliases.getLogicId(), HttpRequestUtils.getAppId(request));
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }

        return templateLogicAliasesManager.deleteTemplateAliases(aliases, HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取appid所有模板的别名", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result<List<Tuple<String/*index*/, String/*aliases*/>>> aliasesList(HttpServletRequest request,
                                                                               @RequestParam("appId") Integer appId) {
        App app = appService.getAppById(appId);
        if (null == app) {
            return Result.buildNotExist("应用不存在");
        }

        return templateLogicAliasesManager.getAllTemplateAliasesByAppid(appId);
    }

    @PostMapping("/aliasSwitch")
    @ApiOperation(value = "别名切换功能", notes = "别名切换功能")
    public Result aliasSwitch(@RequestBody ConsoleTemplateAliasSwitchDTO aliasSwitchDTO) {
        Result result = null;
        try {
            result = templateLogicAliasService.aliasSwitch(aliasSwitchDTO);
        } catch (ESOperateException e) {
            result = Result.buildFail(e.getMessage());
        }
        return result;
    }

}
