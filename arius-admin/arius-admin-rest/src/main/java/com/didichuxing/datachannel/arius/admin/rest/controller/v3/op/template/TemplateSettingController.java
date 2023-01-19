package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplateLogicSettingsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySetting;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3 + "/template/setting")
@Api(tags = "Console-用户侧索引模板setting接口(REST)")
public class TemplateSettingController {

    @Autowired
    private TemplateLogicSettingsManager templateLogicSettingManager;

    @PutMapping("/{logicId}")
    @ResponseBody
    @ApiOperation(value = "更新索引Setting接口")
    public Result<Void> modifySetting(HttpServletRequest request, @PathVariable("logicId") Integer logicId,
                                      @RequestBody String settingDTO) throws AdminOperateException {
        IndexTemplatePhySetting settings = new IndexTemplatePhySetting(JSONObject.parseObject(settingDTO));
        return templateLogicSettingManager.updateSettings(logicId, settings, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取索引Setting接口")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<IndexTemplatePhySetting> getTemplateSettings(@RequestParam("logicId") Integer logicId) throws AdminOperateException {
        return templateLogicSettingManager.getSettings(logicId);
    }

}