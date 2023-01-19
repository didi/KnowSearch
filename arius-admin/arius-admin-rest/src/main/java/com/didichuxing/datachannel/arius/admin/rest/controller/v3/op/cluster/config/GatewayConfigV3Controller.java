package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.config;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterConfigManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.ConfigConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.op.manager.ComponentGroupConfigWithGatewayHostVO;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.interfaces.vo.ComponentGroupConfigVO;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author shizeying
 * @date 2022/10/24
 * @since 0.3.2
 */
@NoArgsConstructor
@RestController()
@RequestMapping(V3 + "/gateway/config")
@Api(tags = "GATEWAY CONFIG管理接口 (REST)")
public class GatewayConfigV3Controller {
    @Autowired
    private GatewayClusterConfigManager gatewayClusterConfigManager;
    @PostMapping("/{gatewayClusterId}/page")
    @ResponseBody
    @ApiOperation(value = "按条件分页获取 gateway 配置列表", tags = "")
    public PaginationResult<GatewayConfigVO> pageGetGatewayConfig(HttpServletRequest request,
        @RequestBody ConfigConditionDTO condition,
        @PathVariable("gatewayClusterId") Integer gatewayClusterId) {
        return gatewayClusterConfigManager.pageGetConfig(condition, HttpRequestUtil.getProjectId(request), gatewayClusterId);
    }
    
    @GetMapping("/{gatewayClusterId}/{configId}")
    @ResponseBody
    @ApiOperation(value = "根据 configId 获取 gateway 配置信息", tags = "")
    public Result<ComponentGroupConfigVO> getConfigByGatewayId(HttpServletRequest request,
                                                                 @PathVariable("gatewayClusterId") Integer gatewayClusterId,
                                                                 @PathVariable("configId") Integer configId) {
        return gatewayClusterConfigManager.getConfigByGatewayId(gatewayClusterId,configId);
    }
    
    @GetMapping("/{gatewayClusterId}/configs")
    @ResponseBody
    @ApiOperation(value = "根据 gatewayClusterId 获取 gateway 配置信息", tags = "")
    public Result<List<ComponentGroupConfigWithGatewayHostVO>> getConfigsByGatewayId(HttpServletRequest request,
        @PathVariable("gatewayClusterId") Integer gatewayClusterId) {
        return gatewayClusterConfigManager.getConfigsByGatewayId(gatewayClusterId);
    }
    
    
    @GetMapping("/{gatewayClusterId}/{configId}/rollback")
    @ResponseBody
    @ApiOperation(value = "根据 gatewayClusterId 和configId获取可以回滚的配置信息", tags = "")
    public Result<List<ComponentGroupConfig>> getRollbackConfigsByClusterPhyId(
        HttpServletRequest request,
        @PathVariable("gatewayClusterId") Integer gatewayClusterId,
        @PathVariable("configId") Integer configId) {
        return gatewayClusterConfigManager.getRollbackConfigsByClusterPhyId(gatewayClusterId, configId);
    }
}