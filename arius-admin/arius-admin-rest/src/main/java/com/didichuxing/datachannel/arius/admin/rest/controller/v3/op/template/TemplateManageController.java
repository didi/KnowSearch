package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chengxiang
 * @date 2022/5/27
 */
@RestController
@RequestMapping(V3_OP + "/template/manage")
@Api(tags = "模板管理接口")
public class TemplateManageController {

    @Autowired
    private TemplateLogicManager templateLogicManager;

    @PutMapping("/createTemplate")
    @ResponseBody
    @ApiOperation(value = "创建逻辑模板")
    public Result<Void> createTemplate(HttpServletRequest request, @RequestBody IndexTemplateWithCreateInfoDTO param) {
        return templateLogicManager.create(param, HttpRequestUtil.getOperator(request), HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/edit")
    @ResponseBody
    @ApiOperation(value = "用户编辑模板")
    public Result<Void> editTemplate(HttpServletRequest request, @RequestBody IndexTemplateDTO param) {
        return templateLogicManager.newEditTemplate(param, HttpRequestUtil.getOperator(request));
    }
}