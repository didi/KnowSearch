package com.didichuxing.datachannel.arius.admin.rest.controller.v3.white.thirdpart;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_THIRD_PART;
import static com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant.GATEWAY_GET_PROJECT_TICKET;
import static com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant.GATEWAY_GET_PROJECT_TICKET_NAME;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.GatewayESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplatePhysicalVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author d06679
 * @date 2019/3/13
 */
@RestController
@RequestMapping(V3_THIRD_PART + "/gateway")
@Api(tags = "第三方gateway接口(REST)")
public class ThirdpartGatewayV3Controller {

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

    @GetMapping("/alive-node-name")
    @ResponseBody
    @ApiOperation(value = "获取gateway存活节点名称列表接口" )
    public Result<List<String>> getGatewayAliveNodeNames(HttpServletRequest request) {
        return gatewayManager.getGatewayAliveNodeNames("Normal");
    }

    @GetMapping("/project")
    @ResponseBody
    @ApiOperation(value = "获取APP列表接口", notes = "获取es user列表,包含es user全部元信息")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-GATEWAY-TICKET", value = "接口ticket", required = true) })
    public Result<List<GatewayESUserVO>> listApp(HttpServletRequest request) {
        String ticket = HttpRequestUtil.getHeaderValue(GATEWAY_GET_PROJECT_TICKET_NAME);
        if (!GATEWAY_GET_PROJECT_TICKET.equals(ticket)) {
            return Result.buildParamIllegal("ticket错误");
        }
        return gatewayManager.listESUserByProject();
    }

    @GetMapping("/template")
    @ResponseBody
    @ApiOperation(value = "获取模板信息", notes = "以map结构组织,key是表达式")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<Map<String, GatewayTemplatePhysicalVO>> getTemplateMap(@RequestParam("cluster") String cluster) {
        return gatewayManager.getTemplateMap(cluster);
    }

    @GetMapping("/deploy-info")
    @ResponseBody
    @ApiOperation(value = "获取模板信息", notes = "主主从结构组织")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "dataCenter", value = "数据中心", required = true) })
    public Result<Map<String, GatewayTemplateDeployInfoVO>> listDeployInfo(@RequestParam(value = "dataCenter") String dataCenter) {
        return gatewayManager.listDeployInfo(dataCenter);
    }

    @PostMapping(path = "dsl/scroll-dsl-template")
    @ApiOperation(value = "滚动获取查询模板数据", notes = "滚动获取查询模板数据", httpMethod = "POST")
    public Result<ScrollDslTemplateResponse> scrollSearchDslTemplate(@ApiParam(name="request", value="滚动查询参数", required = true)
                                                                     @RequestBody ScrollDslTemplateRequest request) {
        return gatewayManager.scrollSearchDslTemplate(request);
    }

    @PostMapping(path = "/alias")
    @ResponseBody
    @ApiOperation(value = "设置一个模板的别名", notes = "设置一个模板的别名")
    public Result<Boolean> addAlias(@RequestBody IndexTemplateAliasDTO indexTemplateAliasDTO){
        return gatewayManager.addAlias(indexTemplateAliasDTO);
    }

    @DeleteMapping(path = "/alias")
    @ResponseBody
    @ApiOperation(value = "删除一个模板的别名", notes = "删除一个模板的别名")
    public Result<Boolean> delAlias(@RequestBody IndexTemplateAliasDTO indexTemplateAliasDTO){
        return gatewayManager.delAlias(indexTemplateAliasDTO);
    }
}