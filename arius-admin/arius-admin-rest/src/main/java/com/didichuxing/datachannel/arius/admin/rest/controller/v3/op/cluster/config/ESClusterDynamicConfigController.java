package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.config;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> getPhyClusterDynamicConfigs(@PathVariable String cluster)
            throws ESOperateException {
        return clusterPhyManager.getPhyClusterDynamicConfigs(cluster);
    }

    @PutMapping("")
    @ApiOperation(value = "更新集群配置项的信息")
    public Result<Boolean> updateDynamicConfig(HttpServletRequest request, @RequestBody ClusterSettingDTO param) throws ESOperateException {
        return clusterPhyManager.updatePhyClusterDynamicConfig(param, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/attributes/{cluster}")
    @ApiOperation(value = "获取当前集群下的属性分配选项")
    public Result<Set<String>> getRoutingAllocationAwarenessAttributes(@PathVariable String cluster) {
        return clusterPhyManager.getRoutingAllocationAwarenessAttributes(cluster);
    }

    @PutMapping("/{clusterList}")
    @ResponseBody
    @ApiOperation(value = "批量更新物理集群的动态配置项")
    public Result<Boolean> batchUpdateClusterDynamicConfig(@PathVariable List<String> clusterList,
                                                           @RequestBody ClusterSettingDTO param,
                                                           HttpServletRequest request) {
        return clusterPhyManager.batchUpdateClusterDynamicConfig(clusterList, param, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }
}