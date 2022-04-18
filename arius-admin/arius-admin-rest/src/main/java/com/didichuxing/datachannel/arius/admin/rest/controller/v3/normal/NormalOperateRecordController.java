package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3_NORMAL + "/record")
@Api(tags = "用户操作记录接口(REST)")
public class NormalOperateRecordController {


    @Autowired
    private OperateRecordService operateRecordService;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "查询操作记录接口", notes = "")
    public Result<List<OperateRecordVO>> list(@RequestBody OperateRecordDTO query) {
        return operateRecordService.list(query);
    }

    @GetMapping("/listModules")
    @ResponseBody
    @ApiOperation(value = "获取所有模块", notes = "")
    public Result<List<Map<String, Object>>> listModules() {
        List<Map<String, Object>> objects = Lists.newArrayList();
        for (ModuleEnum moduleEnum : ModuleEnum.values()) {
            objects.add(moduleEnum.toMap());
        }
        return Result.buildSucc(objects);
    }

    @PostMapping("/{bizId}/{moduleIds}/multiList")
    @ResponseBody
    @ApiOperation(value = "批量查询操作记录接口", notes = "")
    public Result<List<OperateRecordVO>> multiList(@PathVariable("bizId") String bizId,
                                                   @PathVariable("moduleIds") List<Integer> moduleIds) {
        return operateRecordService.multiList(bizId, moduleIds);
    }


}
