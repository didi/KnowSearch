package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import com.didichuxing.datachannel.arius.admin.biz.template.manage.create.TemplateCreateManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateWithCreateInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author chengxiang
 * @date 2022/5/27
 */
@RestController
@RequestMapping(V3_OP + "/template/manage")
@Api(tags = "模板管理接口")
public class TemplateManageController {

    @Autowired
    private TemplateCreateManager templateCreateManager;

    @PutMapping("/createTemplate")
    @ResponseBody
    @ApiOperation(value = "创建逻辑模板")
    public Result<Void> createTemplate(HttpServletRequest request, @RequestBody IndexTemplateWithCreateInfoDTO param) {
        return templateCreateManager.create(param, HttpRequestUtils.getOperator(request), HttpRequestUtils.getAppId(request));
    }
}
