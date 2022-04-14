package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

@RestController
@RequestMapping(V3_NORMAL + "/ecm/machineNorms")
@Api(tags = "ECM-ELASTIC接口(REST)")
public class NormalMachineNormsController {

    @Autowired
    private ESMachineNormsService machineNormsService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "根据类型获取全部机器规格列表接口/类型")
    public Result<List<ESMachineNormsPO>> listMachineNorms(String type) {
        return Result.buildSucc(ConvertUtil.list2List(machineNormsService.listMachineNorms(), ESMachineNormsPO.class));
    }

    @PostMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "获取机器规格详情接口")
    public Result<ESMachineNormsPO> machineNormsDetail(@PathVariable Long id) {
        ESMachineNormsPO machineNormsPO = machineNormsService.getById(id);
        if (AriusObjUtils.isNull(machineNormsPO)) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(machineNormsPO);
    }
}
