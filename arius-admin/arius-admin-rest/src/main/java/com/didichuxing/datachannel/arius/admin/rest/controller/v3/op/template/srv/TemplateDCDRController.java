package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template.srv;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDCDRManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateDCDRInfoVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3_OP + "/template/srv/dcdr")
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
        return templateDCDRManager.copyAndCreateDCDR(templateId, targetCluster, regionId, HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/{templateId}/dcdrInfo")
    @ResponseBody
    @ApiOperation("获取模板DCDR详情")
    public Result<TemplateDCDRInfoVO> getTemplateDCDRInfo(@PathVariable("templateId") Integer templateId) {
        return templateDCDRManager.getTemplateDCDRInfoVO(templateId);
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "DCDR链路删除接口", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true)})
    public Result<Void> deleteDcdr(HttpServletRequest request,
                                   @RequestParam(value = "logicId") Integer logicId) throws AdminOperateException {
        return templateDCDRManager.deleteDCDR(logicId, HttpRequestUtil.getOperator(request));
    }

}