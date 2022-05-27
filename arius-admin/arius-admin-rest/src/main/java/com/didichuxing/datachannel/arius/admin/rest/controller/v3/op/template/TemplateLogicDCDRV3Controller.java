package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDCDRManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalRackDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateDCDRInfoVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(V3_OP + "/template/logic/dcdr")
@Api(tags = "模板Dcdr接口(REST)")
public class TemplateLogicDCDRV3Controller {

    @Autowired
    private TemplateDCDRManager templateDCDRManager;

    @PostMapping("/{templateId}/{targetCluster}")
    @ResponseBody
    @ApiOperation(value = "DCDR链路创建接口", notes = "")
    public Result<Void> createDCDR(HttpServletRequest request, @PathVariable("templateId") Integer templateId,
                                   @PathVariable("targetCluster") String targetCluster, @RequestBody TemplatePhysicalRackDTO templatePhysicalRackDTO) throws AdminOperateException {
        return templateDCDRManager.copyAndCreateDCDR(templateId, targetCluster, templatePhysicalRackDTO.getRack(), HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/{templateId}/dcdrInfo")
    @ResponseBody
    @ApiOperation("获取模板DCDR详情")
    public Result<TemplateDCDRInfoVO> getTemplateDCDRInfo(@PathVariable("templateId") Integer templateId) {
        return templateDCDRManager.getTemplateDCDRInfoVO(templateId);
    }
}