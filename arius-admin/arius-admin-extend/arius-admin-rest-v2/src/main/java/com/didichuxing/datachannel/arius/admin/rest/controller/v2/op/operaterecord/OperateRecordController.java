package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.operaterecord;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;

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

    @GetMapping("/listModules")
    @ResponseBody
    @ApiOperation(value = "获取所有模块" )
    @Deprecated
    public Result<List<Map<String, Object>>> listModules() {
        return Result.buildSucc(ModuleEnum.getAllAriusConfigs());
    }

}
