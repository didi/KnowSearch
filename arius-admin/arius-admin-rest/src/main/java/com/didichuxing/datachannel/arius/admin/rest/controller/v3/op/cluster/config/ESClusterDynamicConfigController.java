package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.config;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsTypeEnum;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3_OP + "/cluster/dynamicConfig")
@Api(tags = "ES集群DynamicConfig接口(REST)")
public class ESClusterDynamicConfigController {

    @Autowired
    private ClusterPhyManager clusterPhyManager;

    @GetMapping("/getAll/{cluster}")
    @ApiOperation(value = "获取当前集群下的动态配置项信息")
    public Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> getPhyClusterDynamicConfigs(@PathVariable String cluster) {
        return clusterPhyManager.getPhyClusterDynamicConfigs(cluster);
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新集群配置项的信息")
    public Result<Boolean> updatePhyClusterDynamicConfig(@RequestBody ClusterSettingDTO param) {
        return clusterPhyManager.updatePhyClusterDynamicConfig(param);
    }

    @GetMapping("/getClusterAttributes/{cluster}")
    @ApiOperation(value = "获取当前集群下的属性分配选项")
    public Result<Set<String>> getRoutingAllocationAwarenessAttributes(@PathVariable String cluster) {
         return clusterPhyManager.getRoutingAllocationAwarenessAttributes(cluster);
    }
}
