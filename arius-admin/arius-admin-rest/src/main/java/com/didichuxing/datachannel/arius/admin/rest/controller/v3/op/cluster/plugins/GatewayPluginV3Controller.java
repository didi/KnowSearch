package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.plugins;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.plugin.PluginManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginInfoTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping(V3 + "/gateway/plugin")
@Api(tags = "GATEWAY plugin管理接口 (REST)")
public class GatewayPluginV3Controller {
    @Autowired
    private PluginManager pluginManager;
    @GetMapping("/{gatewayId}")
    @ResponseBody
    @ApiOperation(value = "获取 gateway 集群安装的插件列表", tags = "")
    public Result<List<PluginVO>> listByGatewayClusterId(HttpServletRequest request,
                                                         @PathVariable("gatewayId") Integer gatewayId) {
        return pluginManager.listGatewayPluginByGatewayClusterId(gatewayId);
    }
    
    @GetMapping("plugin-health")
    @ResponseBody
    @ApiOperation(value = "获取插件健康", tags = "")
    public Result<List<TupleTwo<Integer, PluginHealthEnum>>> getPluginHealth(HttpServletRequest request) {
        return Result.buildSucc(PluginHealthEnum.getAll());
    }
     @GetMapping("plugin-type")
    @ResponseBody
    @ApiOperation(value = "获取插件类型", tags = "")
    public Result<List<TupleTwo<Integer, PluginInfoTypeEnum>>> getPluginInfoType(HttpServletRequest request) {
        return Result.buildSucc(PluginInfoTypeEnum.getAll());
    }
}