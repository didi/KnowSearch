package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplatePhysicalRackDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDcdrManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.TemplateDCDRInfoVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3_OP + "/template/logic/dcdr")
@Api(tags = "模板Dcdr接口(REST)")
public class TemplateLogicDcdrV3Controller {

    @Autowired
    private TemplateDcdrManager  templateDcdrManager;

    @PostMapping("/{templateId}/{targetCluster}")
    @ResponseBody
    @ApiOperation(value = "DCDR链路创建接口", notes = "")
    public Result<Void> createDcdr(HttpServletRequest request, @PathVariable("templateId") Integer templateId,
                                   @PathVariable("targetCluster") String targetCluster, @RequestBody TemplatePhysicalRackDTO templatePhysicalRackDTO) throws AdminOperateException {
        return templateDcdrManager.copyAndCreateDcdr(templateId, targetCluster, templatePhysicalRackDTO.getRack(), HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/{templateId}/dcdrInfo")
    @ResponseBody
    @ApiOperation("获取模板DCDR详情")
    public Result<TemplateDCDRInfoVO> getTemplateDCDRInfo(@PathVariable("templateId") Integer templateId) {
        return templateDcdrManager.getTemplateDCDRInfoVO(templateId);
    }
}
