package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.config;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ESClusterConfigManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.ConfigConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.op.manager.ComponentGroupConfigWithHostVO;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.interfaces.vo.ComponentGroupConfigVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @author lyn
 * @date 2020-12-30
 */
@RestController
@RequestMapping({ V3 + "/cluster/phy/config-file" })
@Api(tags = "ES集群Config接口(REST)")
public class ESClusterConfigController {

    @Autowired
    private ESClusterConfigManager esClusterConfigManager;
    
    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取ES集群配置列表")
    @ApiImplicitParam(paramType = "query", dataType = "String", name = "clusterId", value = "物理集群ID", required = true)
    public Result<List<ESConfigVO>> gainEsClusterConfigs(@RequestParam("clusterId") Long clusterId) {

        return esClusterConfigManager.gainEsClusterConfigs(clusterId);
    }

    @GetMapping("/{configId}")
    @ResponseBody
    @ApiOperation(value = "获取ES集群配置")
    public Result<ESConfigVO> gainEsClusterConfig(@PathVariable Long configId) {
        return esClusterConfigManager.getEsClusterConfigById(configId);
    }

    @GetMapping("/roles/{clusterId}")
    @ResponseBody
    @ApiOperation(value = "获取可操作配置文件的ES集群角色")
    public Result<Set<String>> gainEsClusterRoles(@PathVariable Long clusterId) {

        return esClusterConfigManager.gainEsClusterRoles(clusterId);

    }

    @GetMapping("/template/{type}")
    @ResponseBody
    @ApiOperation(value = "获取ES集群模板配置")
    public Result<ESConfigVO> gainEsClusterTemplateConfig(@PathVariable String type) {

        return esClusterConfigManager.getEsClusterTemplateConfig(type);
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "更新ES集群配置描述")
    public Result<Void> editEsClusterConfigDesc(HttpServletRequest request, @RequestBody ESConfigDTO param) {
        return esClusterConfigManager.editConfigDesc(param, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));

    }
    
    @GetMapping("/{clusterPhyId}/{configId}")
    @ResponseBody
    @ApiOperation(value = "根据 configId 获取物理集群配置信息",tags = "")
    public Result<ComponentGroupConfigVO> getConfigByClusterPhyId(HttpServletRequest request,
                                                                    @PathVariable("clusterPhyId") Integer clusterPhyId,
                                                                    @PathVariable("configId") Integer configId) {
       return esClusterConfigManager.getConfigByClusterPhyId(clusterPhyId,configId);
    }
    
    @PostMapping("/{phyClusterId}/config/page")
    @ResponseBody
    @ApiOperation(value = "按条件分页获取 cluster 配置列表", tags = "")
    public PaginationResult<ClusterPhyConfigVO> pageGetGatewayConfig(HttpServletRequest request,
                                                                     @RequestBody ConfigConditionDTO condition,@PathVariable("phyClusterId") Integer phyClusterId) {
        return esClusterConfigManager.pageGetConfig(condition, HttpRequestUtil.getProjectId(request), phyClusterId);
    }
    
    @GetMapping("/{clusterPhyId}/configs")
    @ResponseBody
    @ApiOperation(value = "根据 clusterPhyId 获取物理集群配置信息", tags = "")
    public Result<List<ComponentGroupConfigWithHostVO>> getConfigsByClusterPhyId(
        HttpServletRequest request,
        @PathVariable("clusterPhyId") Integer clusterPhyId) {
        return esClusterConfigManager.getConfigsByClusterPhyId(clusterPhyId);
    }
    
    @GetMapping("/{clusterPhyId}/{configId}/rollback")
    @ResponseBody
    @ApiOperation(value = "根据 clusterPhyId 和configId获取可以回滚的配置信息", tags = "")
    public Result<List<ComponentGroupConfig>> getRollbackConfigsByClusterPhyId(
        HttpServletRequest request,
        @PathVariable("clusterPhyId") Integer clusterPhyId,
        @PathVariable("configId") Integer configId) {
        return esClusterConfigManager.getRollbackConfigsByClusterPhyId(clusterPhyId, configId);
    }
    
    
    
}