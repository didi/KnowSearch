package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplateLogicSettingsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySetting;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(V2_CONSOLE + "/template/setting")
@Api(tags = "Console-用户侧索引模板setting接口(REST)：见："+V3_OP)
@Deprecated
public class ConsoleTemplateSettingController extends BaseConsoleTemplateController {

    @Autowired
    private TemplateLogicSettingsManager templateLogicSettingManager;

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "更新索引Setting接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-APP-ID", value = "应用ID", required = true) })
    @Deprecated
    public Result<Void> modifySetting(HttpServletRequest request,
                                @RequestBody ConsoleTemplateSettingDTO settingDTO) throws AdminOperateException {
        Result<Void> checkAuthResult = checkAppAuth(settingDTO.getLogicId(), HttpRequestUtil.getProjectId(request));
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }

        //return Result.buildFail("已经废弃");
         return Result.buildFail("接口已经下线：迁移到v3");
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取索引Setting接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<IndexTemplatePhySetting> getTemplateSettings(@RequestParam("logicId") Integer logicId) throws AdminOperateException {
        //return templateLogicSettingsManager.getSettings(logicId);
         return Result.buildFail("接口已经下线：迁移到v3");
    }
}