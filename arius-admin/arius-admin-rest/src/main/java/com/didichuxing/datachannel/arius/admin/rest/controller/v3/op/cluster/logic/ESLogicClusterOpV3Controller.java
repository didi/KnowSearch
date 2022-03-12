package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;


/**
 * @author lanxinzheng
 * @date 2020/10/22
 */
@RestController
@RequestMapping(V3_OP + "/logic/cluster")
@Api(tags = "ES逻辑集群接口(REST)")
public class ESLogicClusterOpV3Controller {

    @Autowired
    private ClusterLogicManager clusterLogicManager;

    @GetMapping("/clusterNames")
    @ResponseBody
    @ApiOperation(value = "根据AppId获取逻辑集群下的逻辑集群名称")
    public Result<List<String>> getAppLogicClusterNames(HttpServletRequest request) {
        return clusterLogicManager.getAppLogicClusterNames(HttpRequestUtils.getAppId(request));
    }

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "根据AppId获取有权限的逻辑集群信息")
    public Result<List<ConsoleClusterVO>> getAppLogicClusterInfo(HttpServletRequest request) {
        return clusterLogicManager.getAppLogicClusterInfo(HttpRequestUtils.getAppId(request));
    }

    @GetMapping("/{type}")
    @ResponseBody
    @ApiOperation(value = "根据项目和集群类型获取逻辑集群(项目对其有管理权限)名称列表")
    public Result<List<ConsoleClusterVO>> getAppLogicClusterInfoByType(HttpServletRequest request, @PathVariable Integer type) {
        return clusterLogicManager.getAppLogicClusterInfoByType(HttpRequestUtils.getAppId(request), type);
    }

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "条件获取逻辑集群列表")
    public PaginationResult<ConsoleClusterVO> pageGetConsoleClusterVOS(HttpServletRequest request,
                                                                                @RequestBody ClusterLogicConditionDTO condition) {
        return clusterLogicManager.pageGetConsoleClusterVOS(condition, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("/{clusterLogicId}/overView")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群概览信息接口")
    @ApiImplicitParam(type = "Long", name = "clusterLogicId", value = "逻辑集群ID", required = true)
    public Result<ConsoleClusterVO> get(HttpServletRequest request, @PathVariable Long clusterLogicId) {
        return Result.buildSucc(clusterLogicManager.getConsoleCluster(clusterLogicId, HttpRequestUtils.getAppId(request)));
    }
}
