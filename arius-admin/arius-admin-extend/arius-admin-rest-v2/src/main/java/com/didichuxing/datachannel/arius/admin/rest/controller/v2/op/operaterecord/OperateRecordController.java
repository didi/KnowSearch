package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.operaterecord;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.NewModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import java.util.List;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author d06679
 * @date 2017/10/9
 */
@RestController
@RequestMapping(V2_OP + "/record")
@Api(tags = "运维操作记录接口(REST)")
public class OperateRecordController {

    @GetMapping("/module")
    @ResponseBody
    @ApiOperation(value = "获取所有模块" )
    public Result<Map<Integer, String>> mapModules() {
        return Result.buildSucc(NewModuleEnum.toMap());
    }
    @GetMapping("/operation-type/{moduleCode}")
    @ResponseBody
    @ApiOperation(value = "获取操作类型" )
    @ApiImplicitParams({
           @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "moduleCode", value = "模块code:为空则会返回全部",
                   required =
                   true)
    })
    public Result<List<String>> listOperationType(@PathVariable("moduleCode") Integer moduleCode) {
        return Result.buildSucc(OperationTypeEnum.getOperationTypeByModule(moduleCode));
    }
    
    @GetMapping("/trigger-way")
    @ResponseBody
    @ApiOperation(value = "获取触发方式")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "moduleCode", value = "模块code:为空则会返回全部", required = true) })
    public Result<List<String>> listTriggerWay() {
        return Result.buildSucc(TriggerWayEnum.getOperationList());
    }

}