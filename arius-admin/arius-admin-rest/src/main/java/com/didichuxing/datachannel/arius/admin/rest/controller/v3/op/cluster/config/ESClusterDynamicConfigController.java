package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.config;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.MultiClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author ohushenglin_v
 * @date 2022-05-30
 */
@RestController
@RequestMapping({ V3 + "/cluster/phy/dynamic-config" })
@Api(tags = "ES集群DynamicConfig接口(REST)")
public class ESClusterDynamicConfigController {

    @Autowired
    private ClusterPhyManager clusterPhyManager;

    @GetMapping("/{cluster}")
    @ApiOperation(value = "获取当前集群下的动态配置项信息")
    public Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> getPhyClusterDynamicConfigs(@PathVariable String cluster) {
        return clusterPhyManager.getPhyClusterDynamicConfigs(cluster);
    }

    @PutMapping("")
    @ApiOperation(value = "更新集群配置项的信息")
    public Result<Boolean> updateDynamicConfig(HttpServletRequest request, @RequestBody ClusterSettingDTO param) {
        return clusterPhyManager.updatePhyClusterDynamicConfig(param, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/attributes/{cluster}")
    @ApiOperation(value = "获取当前集群下的属性分配选项")
    public Result<Set<String>> getRoutingAllocationAwarenessAttributes(@PathVariable String cluster) {
        return clusterPhyManager.getRoutingAllocationAwarenessAttributes(cluster);
    }

    @PutMapping("/multi-cluster")
    @ResponseBody
    @ApiOperation(value = "批量更新物理集群的动态配置项")
    public Result<Boolean> batchUpdateClusterDynamicConfig(@RequestBody MultiClusterSettingDTO param,
                                                           HttpServletRequest request) throws ESOperateException {
        return clusterPhyManager.batchUpdateClusterDynamicConfig(param, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }
}