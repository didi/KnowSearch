package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.gateway;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.ConfigConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.GatewayJoin;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayConfigVO;
import com.didiglobal.logi.op.manager.interfaces.vo.GeneralGroupConfigHostVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网关 v3 控制器
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */
@NoArgsConstructor
@RestController()
@RequestMapping(V3 + "/gateway")
@Api(tags = "GATEWAY 管理接口 (REST)")
public class GatewayV3Controller {
    
    @GetMapping("/brief-info")
    @ApiOperation(value = "gateway 管理简要信息", notes = "")
    public Result<List<GatewayClusterBriefVO>> listBriefInfo(HttpServletRequest request) {
        return Result.buildSucc(Collections.singletonList(new GatewayClusterBriefVO()));
    }
    
    @PostMapping("/join")
    @ApiOperation(value = "gateway 集群接入", notes = "")
    public Result<GatewayClusterVO> joinCluster(HttpServletRequest request, @RequestBody GatewayClusterJoinDTO param) {
        return Result.buildSucc(new GatewayClusterVO());
    }
    
    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "按条件分页获取 gateway 集群集群列表")
    public PaginationResult<GatewayClusterVO> pageGetGatewayCluster(HttpServletRequest request,
                                                                    @RequestBody GatewayConditionDTO condition) {
        return new PaginationResult<>(Collections.singletonList(new GatewayClusterVO()), 10, 1, 10);
    }
    
    @PostMapping("/node/page")
    @ResponseBody
    @ApiOperation(value = "按条件分页获取 gateway 节点集群列表")
    public PaginationResult<GatewayClusterNodeVO> pageGetGatewayNodes(HttpServletRequest request,
                                                                      @RequestBody GatewayNodeConditionDTO condition) {
        return new PaginationResult<>(Collections.singletonList(new GatewayClusterNodeVO()), 10, 1, 10);
    }
    
    @PostMapping("/config/page")
    @ResponseBody
    @ApiOperation(value = "按条件分页获取 gateway 配置列表")
    public PaginationResult<GatewayConfigVO> pageGetGatewayConfig(HttpServletRequest request,
                                                                  @RequestBody ConfigConditionDTO condition) {
        return new PaginationResult<>(Collections.singletonList(new GatewayConfigVO()), 10, 1, 10);
    }
    
    @GetMapping("/{gatewayClusterId}")
    @ResponseBody
    @ApiOperation(value = "根据 gatewayClusterId 获取 gateway 集群信息")
    public Result<GatewayClusterVO> getGateway(HttpServletRequest request,
                                               @PathVariable("gatewayClusterId") Integer gatewayClusterId) {
        return Result.buildSucc(new GatewayClusterVO());
    }
    
    
  
    
    @DeleteMapping("/{gatewayClusterId}")
    @ResponseBody
    @ApiOperation(value = "gateway 集群下线")
    public Result<Void> deleteById(HttpServletRequest request,
                                                       @PathVariable("gatewayClusterId") Integer gatewayClusterId) {
        return Result.buildSucc();
    }
    
    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "gateway 编辑")
    public Result<Void> edit(HttpServletRequest request, @RequestBody GatewayDTO data) {
        return Result.buildSucc();
    }
    
    @PostMapping("/{gatewayId}")
    @ResponseBody
    @ApiOperation(value = "获取 gateway 集群安装的插件列表")
    public Result<PluginVO> listByGatewayClusterId(HttpServletRequest request,
                                                   @PathVariable("gatewayId") Integer gatewayId) {
        return Result.buildSucc();
    }
    
    @GetMapping("/{gatewayClusterId}/{configId}")
    @ResponseBody
    @ApiOperation(value = "根据 configId 获取 gateway 配置信息")
    public Result<GeneralGroupConfigHostVO> getConfigByGatewayId(HttpServletRequest request,
                                                                 @PathVariable("gatewayClusterId") Integer gatewayClusterId,
                                                                 @PathVariable("configId") Integer configId) {
        return Result.buildSucc(new GeneralGroupConfigHostVO());
    }
    
}