package com.didichuxing.datachannel.arius.admin.rest.controller.v2.thirdpart;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_THIRD_PART;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.GatewayAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplatePhysicalVO;

import io.swagger.annotations.*;

/**
 *
 * @author d06679
 * @date 2019/3/13
 */
@RestController
@RequestMapping(V2_THIRD_PART + "/gateway")
@Api(tags = "第三方gateway接口(REST)")
public class ThirdpartGatewayController {

    @Autowired
    private GatewayManager gatewayManager;

    @PutMapping("/heartbeat")
    @ResponseBody
    @ApiOperation(value = "gateway心跳接口" )
    public Result<Void> heartbeat(@RequestBody GatewayHeartbeat heartbeat) {
        return gatewayManager.heartbeat(heartbeat);
    }

    @GetMapping("/alivecount")
    @ResponseBody
    @ApiOperation(value = "gateway存活接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "clusterName", value = "集群名称", required = true) })
    public Result<Integer> heartbeat(@RequestParam(value = "clusterName") String clusterName) {
        return gatewayManager.heartbeat(clusterName);
    }

    @GetMapping("/aliveNodeName")
    @ResponseBody
    @ApiOperation(value = "获取gateway存活节点名称列表接口" )
    public Result<List<String>> getGatewayAliveNodeNames(HttpServletRequest request) {
        return gatewayManager.getGatewayAliveNodeNames("Normal");
    }

    @GetMapping("/listApp")
    @ResponseBody
    @ApiOperation(value = "获取APP列表接口", notes = "获取app列表,包含APP全部元信息")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-GATEWAY-TICKET", value = "接口ticket", required = true) })
    public Result<List<GatewayAppVO>> listApp(HttpServletRequest request) {
        return gatewayManager.listApp(request);
    }

    @GetMapping("/getTemplateMap")
    @ResponseBody
    @ApiOperation(value = "获取模板信息", notes = "以map结构组织,key是表达式")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<Map<String, GatewayTemplatePhysicalVO>> getTemplateMap(@RequestParam("cluster") String cluster) {
        return gatewayManager.getTemplateMap(cluster);
    }

    @GetMapping("/listDeployInfo")
    @ResponseBody
    @ApiOperation(value = "获取模板信息", notes = "主主从结构组织")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "dataCenter", value = "数据中心", required = true) })
    public Result<Map<String, GatewayTemplateDeployInfoVO>> listDeployInfo(@RequestParam(value = "dataCenter") String dataCenter) {
        return gatewayManager.listDeployInfo(dataCenter);
    }

    @PostMapping(path = "dsl/scrollDslTemplates")
    @ApiOperation(value = "滚动获取查询模板数据", notes = "滚动获取查询模板数据", httpMethod = "POST")
    public Result<ScrollDslTemplateResponse> scrollSearchDslTemplate(@ApiParam(name="request", value="滚动查询参数", required = true)
                                                                     @RequestBody ScrollDslTemplateRequest request) {
        return gatewayManager.scrollSearchDslTemplate(request);
    }

    @PostMapping(path = "/addAlias")
    @ResponseBody
    @ApiOperation(value = "设置一个模板的别名", notes = "设置一个模板的别名")
    public Result<Boolean> addAlias(@RequestBody IndexTemplateAliasDTO indexTemplateAliasDTO){
        return gatewayManager.addAlias(indexTemplateAliasDTO);
    }

    @PostMapping(path = "/delAlias")
    @ResponseBody
    @ApiOperation(value = "删除一个模板的别名", notes = "删除一个模板的别名")
    public Result<Boolean> delAlias(@RequestBody IndexTemplateAliasDTO indexTemplateAliasDTO){
        return gatewayManager.delAlias(indexTemplateAliasDTO);
    }
}
