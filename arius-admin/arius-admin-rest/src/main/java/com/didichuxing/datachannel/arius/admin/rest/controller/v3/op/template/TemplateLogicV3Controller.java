package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Created by linyunan on 2021-07-30
 */
@RestController
@RequestMapping(V3_OP + "/template/logic")
@Api(tags = "逻辑模板接口(REST)")
public class TemplateLogicV3Controller {

    @Autowired
    private TemplateLogicManager templateLogicManager;

    @GetMapping("/listNames")
    @ResponseBody
    @ApiOperation(value = "获取逻辑模板名称列表接口")
    public Result<List<String>> listTemplateLogicNames(HttpServletRequest request) {
        return Result.buildSucc(templateLogicManager.getTemplateLogicNames(HttpRequestUtils.getAppId(request)));
    }

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "模糊查询模板列表")
    public PaginationResult<ConsoleTemplateVO> pageGetConsoleTemplateVOS(HttpServletRequest request,
                                                                         @RequestBody TemplateConditionDTO condition) {
        return templateLogicManager.pageGetConsoleTemplateVOS(condition, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("/{templateName}/nameCheck")
    @ResponseBody
    @ApiOperation(value = "校验模板名称是否合法")
    public Result<Void> checkTemplateValidForCreate(@PathVariable("templateName") String templateName) {
        return templateLogicManager.checkTemplateValidForCreate(templateName);
    }

    @PostMapping("/sizeCheck")
    @ResponseBody
    @ApiOperation(value = "校验模板大小资源是否充足")
    public Result<Void> checkTemplateValidForCreate(@RequestBody IndexTemplateLogicDTO param) {
        return templateLogicManager.checkTemplateDataSizeValidForCreate(param);
    }

    @GetMapping("/{templateId}/checkEditMapping/")
    @ResponseBody
    @ApiOperation(value = "校验可否编辑模板mapping")
    public Result<Boolean> checkTemplateEditMapping(@PathVariable Integer templateId) {
        return templateLogicManager.checkTemplateEditMapping(templateId);
    }
}
