package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplateLogicSettingsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySettings;
import com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.template.BaseConsoleTemplateController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3 + "/template/setting")
@Api(tags = "索引模板setting接口(REST)")
public class TemplateSettingController extends BaseConsoleTemplateController {

    @Autowired
    private TemplateLogicSettingsManager templateLogicSettingsManager;
    @PutMapping("/{logicId}")
    @ResponseBody
    @ApiOperation(value = "更新索引Setting接口" )
    public Result<Void> modifySetting(@PathVariable("logicId") Integer logicId,
                                      @RequestBody String settingDTO) {
        IndexTemplatePhySettings settings = new IndexTemplatePhySettings(JSONObject.parseObject(settingDTO));
        return templateLogicSettingsManager.updateSettings(logicId, settings);
    }
}
