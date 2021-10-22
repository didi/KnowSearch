package com.didichuxing.datachannel.arius.admin.rest.controller.v3.thirdpart;

import com.didichuxing.datachannel.arius.admin.biz.workorder.WorkOrderManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_THIRD_PART;

@RestController
@RequestMapping(V3_THIRD_PART + "/plugin")
@Api(value = "第三方访问接口--自定义插件信息(REST)")
public class ThirdpartPluginController {

    @Autowired
    private WorkOrderManager workOrderManager;

    // todo 路径改rest风格
    @GetMapping(path = "/info")
    @ResponseBody
    @ApiOperation(value = "获取宙斯ES执行脚本插件信息", notes = "")
    @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster_name", value = "集群名称", required = true)
    // todo cluster_name改clusterName
    public String getPluginDetail(@RequestParam(value = "cluster_name") String cluster) {
        return workOrderManager.getClusterTaskInfo(cluster).getData();
    }
}
