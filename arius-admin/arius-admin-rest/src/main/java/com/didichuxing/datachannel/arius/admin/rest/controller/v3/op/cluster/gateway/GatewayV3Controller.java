package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.gateway;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterManager;
import com.didichuxing.datachannel.arius.admin.biz.plugin.PluginManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.software.PackageBriefVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NoArgsConstructor;

/**
 * 网关 v3 控制器
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 
 */
@NoArgsConstructor
@RestController()
@RequestMapping(V3 + "/gateway")
@Api(tags = "GATEWAY 管理接口 (REST)")
public class GatewayV3Controller {
    @Autowired
    private GatewayClusterManager gatewayClusterManager;
    @Autowired
    private PluginManager         pluginManager;
    @GetMapping("/brief-info")
    @ApiOperation(value = "gateway 管理简要信息", tags = "")
    public Result<List<GatewayClusterBriefVO>> listBriefInfo(HttpServletRequest request) {
        return gatewayClusterManager.listBriefInfo();
    }
    
    @PostMapping("/join")
    @ApiOperation(value = "gateway 集群接入", tags = "")
    public Result<GatewayClusterVO> joinCluster(HttpServletRequest request, @RequestBody GatewayClusterJoinDTO param) {
        return gatewayClusterManager.join(param, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request));
    }
    
    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "按条件分页获取 gateway 集群集群列表",tags = "")
    public PaginationResult<GatewayClusterVO> pageGetGatewayCluster(HttpServletRequest request,
                                                                    @RequestBody GatewayConditionDTO condition) {
        return gatewayClusterManager.pageGetCluster(condition,HttpRequestUtil.getProjectId(request));
    }
    
    @GetMapping("/{gatewayClusterId}")
    @ResponseBody
    @ApiOperation(value = "根据 gatewayClusterId 获取 gateway 集群信息",tags = "")
    public Result<GatewayClusterVO> getGateway(HttpServletRequest request,
                                               @PathVariable("gatewayClusterId") Integer gatewayClusterId) {
        return gatewayClusterManager.getOneById(gatewayClusterId);
    }
    
    @DeleteMapping("/{gatewayClusterId}")
    @ResponseBody
    @ApiOperation(value = "gateway 集群下线",tags = "")
    public Result<Void> deleteById(HttpServletRequest request,
                                                       @PathVariable("gatewayClusterId") Integer gatewayClusterId) {
        return gatewayClusterManager.deleteById(gatewayClusterId,
            HttpRequestUtil.getProjectId(request),HttpRequestUtil.getOperator(request));
    }
    
    @PutMapping("/{gatewayClusterId}")
    @ResponseBody
    @ApiOperation(value = "gateway 编辑",tags = "")
    public Result<Void> edit(HttpServletRequest request, @PathVariable("gatewayClusterId") Integer gatewayClusterId, @RequestBody GatewayClusterDTO data) {
        data.setId(gatewayClusterId);
        return gatewayClusterManager.editOne(data,HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request));
    }
    @GetMapping("/{gatewayClusterId}/lower-versions")
    @ResponseBody
    @ApiOperation(value = "获取更低的版本号",tags = "")
    public Result<List<PackageBriefVO>> getLowerVersionByGatewayClusterId(HttpServletRequest request,
                                                                          @PathVariable("gatewayClusterId") Integer gatewayClusterId) {
        return pluginManager.getLowerVersionByGatewayClusterId(gatewayClusterId);
    }
    
    @GetMapping("health")
    @ResponseBody
    @ApiOperation(value = "获取健康类型", tags = "")
    public Result<List<TupleTwo<Integer, PluginHealthEnum>>> getPluginHealth(HttpServletRequest request) {
        return Result.buildSucc(PluginHealthEnum.getAll());
    }
  
    
}