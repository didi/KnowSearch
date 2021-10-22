package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Label;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplateLabelDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.IndexTemplateLogicWithLabelVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @author d06679
 * @date 2019/5/20
 */
@RestController
@RequestMapping(V2_OP + "/template/label")
@Api(value = "es集群模板接口(REST)")
public class TemplateLabelController {

    @Autowired
    private TemplateLogicManager templateLogicManager;

    @Autowired
    private TemplateLabelService templateLabelService;

    @GetMapping("/listAllLabel")
    @ResponseBody
    @ApiOperation(value = "获取所有标签接口", notes = "")
    public Result<List<Label>> listAllLabel() {
        return templateLabelService.listAllLabel();
    }

    @PutMapping("/save")
    @ResponseBody
    @ApiOperation(value = "保存模板标签", notes = "")
    public Result save(HttpServletRequest request, @RequestBody TemplateLabelDTO templateLabel) {
        return templateLabelService.replaceTemplateLabel(templateLabel.getTemplateId(),
            templateLabel.getTemplateLabel(), HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/listByLabelIds")
    @ResponseBody
    @ApiOperation(value = "获取逻辑模板(标签)列表接口")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "includeLabelIds", value = "包含的标签id"),
                         @ApiImplicitParam(paramType = "query", dataType = "String", name = "excludeLabelIds", value = "排除的标签id") })
    public Result<List<IndexTemplateLogicWithLabelVO>> listByLabelIds(@RequestParam(name = "includeLabelIds", required = false) String includeLabelIds,
                                                                      @RequestParam(name = "excludeLabelIds", required = false) String excludeLabelIds) {
        return Result.buildSucc(ConvertUtil.list2List(templateLogicManager.getByLabelIds(includeLabelIds, excludeLabelIds),
            IndexTemplateLogicWithLabelVO.class));
    }

    @GetMapping("/getByLogicId")
    @ResponseBody
    @ApiOperation(value = "获取逻辑模板(标签)列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "logicId", value = "逻辑模板ID", required = true) })
    public Result<IndexTemplateLogicWithLabelVO> getByLogicId(@RequestParam(value = "logicId") Integer logicId) {
        return Result.buildSucc(
            ConvertUtil.obj2Obj(templateLogicManager.getLabelByLogicId(logicId), IndexTemplateLogicWithLabelVO.class));
    }

}
