package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterVO;
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
        return clusterLogicManager.getAppLogicClusterNames(HttpRequestUtils.getProjectId(request));
    }

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "根据AppId获取有权限的逻辑集群信息")
    public Result<List<ConsoleClusterVO>> getAppLogicClusterInfo(HttpServletRequest request) {
        return clusterLogicManager.getAppLogicClusterInfo(HttpRequestUtils.getProjectId(request));
    }

    @GetMapping("/{type}")
    @ResponseBody
    @ApiOperation(value = "根据项目和集群类型获取逻辑集群(项目对其有管理权限)名称列表")
    public Result<List<ConsoleClusterVO>> getAppLogicClusterInfoByType(HttpServletRequest request, @PathVariable Integer type) {
        return clusterLogicManager.getAppLogicClusterInfoByType(HttpRequestUtils.getProjectId(request), type);
    }

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "条件获取逻辑集群列表")
    public PaginationResult<ConsoleClusterVO> pageGetConsoleClusterVOS(HttpServletRequest request,
                                                                                @RequestBody ClusterLogicConditionDTO condition) {
        return clusterLogicManager.pageGetConsoleClusterVOS(condition, HttpRequestUtils.getProjectId(request));
    }

    @GetMapping("/{clusterLogicId}/overView")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群概览信息接口")
    @ApiImplicitParam(type = "Long", name = "clusterLogicId", value = "逻辑集群ID", required = true)
    public Result<ConsoleClusterVO> get(HttpServletRequest request, @PathVariable Long clusterLogicId) {
        return Result.buildSucc(clusterLogicManager.getConsoleCluster(clusterLogicId, HttpRequestUtils.getProjectId(request)));
    }

    @GetMapping("/{logicClusterId}/{templateSize}/sizeCheck")
    @ResponseBody
    @ApiOperation(value = "校验模板大小资源是否充足,主要是为了避免用户反复的进行模板创建操作，对于申请的权限做一定的限制")
    public Result<Void> checkTemplateValidForCreate(@PathVariable("logicClusterId") Long logicClusterId,
                                                    @PathVariable("templateSize") String templateSize) {
        //TODO: wpk 性能优化， 针对集群模板上万的场景
        //return clusterLogicManager.checkTemplateDataSizeValidForCreate(logicClusterId, templateSize);
        return Result.buildSucc();
    }

}