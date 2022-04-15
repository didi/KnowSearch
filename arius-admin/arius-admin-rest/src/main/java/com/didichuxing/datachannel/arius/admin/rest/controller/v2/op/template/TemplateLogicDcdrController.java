package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDcdrManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V2_OP + "/template/logic/dcdr")
@Api(tags = "es集群逻辑模板Dcdr接口(REST)")
public class TemplateLogicDcdrController {

    @Autowired
    private TemplateLogicService templateLogicService;

    @Autowired
    private TemplateDcdrManager templateDcdrManager;

    @PostMapping("/switchMasterSlave")
    @ResponseBody
    @ApiOperation(value = "DCDR主从切换接口", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "Long", name = "expectMasterPhysicalId", value = "期望的主", required = true)})
    public Result<Void> dcdrSwitchMasterSlave(HttpServletRequest request, @RequestParam(value = "logicId") Integer logicId,
                                        @RequestParam(value = "expectMasterPhysicalId") Long expectMasterPhysicalId) {
        String operator = HttpRequestUtils.getOperator(request);
        Result<Void> result = templateDcdrManager.dcdrSwitchMasterSlave(logicId, expectMasterPhysicalId, 1, operator);
        return result;
    }

    @GetMapping("/switchMasterSlaveWithStep")
    @ResponseBody
    @ApiOperation(value = "DCDR主从切换接口", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "Long", name = "expectMasterPhysicalId", value = "期望的主", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "String", name = "operator", value = "操作人", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "step", value = "执行进度", required = true)})
    public Result<Void> switchMasterSlaveWithStep(@RequestParam(value = "logicId") Integer logicId,
                                            @RequestParam(value = "expectMasterPhysicalId") Long expectMasterPhysicalId,
                                            @RequestParam(value = "operator") String operator,
                                            @RequestParam(value = "step") int step) {
        Result<Void> result = templateDcdrManager.dcdrSwitchMasterSlave(logicId, expectMasterPhysicalId, step, operator);
        return result;
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "DCDR链路创建接口", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true)})
    @Deprecated
    public Result<Void> createDcdr(HttpServletRequest request,
                             @RequestParam(value = "logicId") Integer logicId) throws AdminOperateException {
        return templateDcdrManager.createDcdr(logicId, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "DCDR链路删除接口", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true)})
    public Result<Void> deleteDcdr(HttpServletRequest request,
                             @RequestParam(value = "logicId") Integer logicId) throws AdminOperateException {
        return templateDcdrManager.deleteDcdr(logicId, HttpRequestUtils.getOperator(request));
    }
}
