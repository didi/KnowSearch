package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean.OdinTreeNode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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
        if (ValidateUtils.isNull(machineNormsPO)) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(machineNormsPO);
    }
}
