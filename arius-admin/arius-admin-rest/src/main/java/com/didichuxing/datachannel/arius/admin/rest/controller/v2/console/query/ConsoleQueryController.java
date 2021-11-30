package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.query;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateLabel;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author d06679
 * @date 2019/5/15
 */
@RestController
@RequestMapping(V2_CONSOLE + "/query")
@Api(tags = "Console-用户侧查询分析接口(REST)")
public class ConsoleQueryController {


    @Autowired
    private TemplateLabelService templateLabelService;

    @Autowired
    private TemplateLogicService       templateLogicService;

    @GetMapping("/listDslReviewTemplate")
    @ResponseBody
    @ApiOperation(value = "获取需要申请dsl的模板", notes = "")
    public Result<List<ConsoleTemplateVO>> listNeedAuth() {
        Result<List<TemplateLabel>> result = templateLabelService.listDslReviewTemplates();
        if (result.failed()) {
            return Result.buildFail("获取索引失败");
        }

        List<ConsoleTemplateVO> templateVOS = Lists.newArrayList();

        for (TemplateLabel templateLabel : result.getData()) {
            ConsoleTemplateVO templateVO = ConvertUtil
                .obj2Obj(templateLogicService.getLogicTemplateById(templateLabel.getIndexTemplateId()), ConsoleTemplateVO.class);

            templateVOS.add(templateVO);
        }

        return Result.buildSucc(templateVOS);
    }
}
