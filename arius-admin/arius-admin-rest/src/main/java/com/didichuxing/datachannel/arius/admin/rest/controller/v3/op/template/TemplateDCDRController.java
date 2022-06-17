package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDCDRManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateDCDRInfoVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping({ V3_OP + "/template/dcdr", V3 + "/template/dcdr" })
@Api(tags = "模板DCDR接口(REST)")
public class TemplateDCDRController {

    @Autowired
    private TemplateDCDRManager templateDCDRManager;

    @PostMapping("/{templateId}/{regionId}/{targetCluster}")
    @ResponseBody
    @ApiOperation(value = "DCDR链路创建接口", notes = "")
    public Result<Void> createDCDR(HttpServletRequest request,
                                   @PathVariable("templateId") Integer templateId,
                                   @PathVariable("targetCluster") String targetCluster,
                                   @PathVariable("regionId") Integer regionId) throws AdminOperateException {
        return templateDCDRManager.copyAndCreateDCDR(templateId, targetCluster, regionId, HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/{templateId}/dcdrInfo")
    @ResponseBody
    @ApiOperation("获取模板DCDR详情")
    public Result<TemplateDCDRInfoVO> getTemplateDCDRInfo(@PathVariable("templateId") Integer templateId) {
        return templateDCDRManager.getTemplateDCDRInfoVO(templateId);
    }

    @DeleteMapping("/{templateId}")
    @ResponseBody
    @ApiOperation(value = "DCDR链路删除接口", notes = "")
    public Result<Void> deleteDcdr(HttpServletRequest request, @PathVariable(value = "templateId") Integer templateId) throws AdminOperateException {
        return templateDCDRManager.deleteDCDR(templateId, HttpRequestUtils.getOperator(request));
    }

}
