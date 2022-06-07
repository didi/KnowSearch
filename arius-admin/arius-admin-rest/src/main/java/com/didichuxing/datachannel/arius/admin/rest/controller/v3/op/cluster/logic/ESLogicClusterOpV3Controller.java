package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicTemplateIndexCountVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicNodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author guoyoupeng_v
 * @date 2022/05/25
 */
@RestController
@RequestMapping({ V3_OP + "/logic/cluster", V3 + "/cluster/logic" })
@Api(tags = "ES我的集群接口(REST)")
public class ESLogicClusterOpV3Controller {
    
    @Autowired
    private ClusterLogicManager clusterLogicManager;
    
    @Autowired
    private ClusterNodeManager clusterNodeManager;
    
    @Autowired
    private ClusterLogicNodeService clusterLogicNodeService;
    
    @GetMapping("/names")
    @ResponseBody
    @ApiOperation(value = "根据AppId获取逻辑集群下的逻辑集群名称")
    public Result<List<String>> getAppLogicOrPhysicClusterNames(HttpServletRequest request) {
        return clusterLogicManager.getAppLogicOrPhysicClusterNames(HttpRequestUtils.getAppId(request));
    }
    
    @GetMapping()
    @ResponseBody
    @ApiOperation(value = "根据AppId获取有权限的逻辑或物理集群信息")
    public Result<List<ClusterLogicVO>> getAppLogicClusterInfo(HttpServletRequest request) {
        return clusterLogicManager.getAppLogicClusterInfo(HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "条件获取逻辑集群列表")
    public PaginationResult<ClusterLogicVO> pageGetClusterLogicVOS(HttpServletRequest request,
                                                                     @RequestBody ClusterLogicConditionDTO condition) {
        return clusterLogicManager.pageGetClusterLogicVOS(condition, HttpRequestUtils.getAppId(request));
    }
    
    @GetMapping("/detail/{clusterLogicId}")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群概览信息接口")
    @ApiImplicitParam(type = "Long", name = "clusterLogicId", value = "逻辑集群ID", required = true)
    public Result<ClusterLogicVO> detail(HttpServletRequest request, @PathVariable Long clusterLogicId) {
        return Result.buildSucc(
                clusterLogicManager.getConsoleCluster(clusterLogicId, HttpRequestUtils.getAppId(request)));
    }
    
    @GetMapping("/{logicClusterId}/{templateSize}/sizeCheck")
    @ResponseBody
    @ApiOperation(value = "校验模板大小资源是否充足,主要是为了避免用户反复的进行模板创建操作，对于申请的权限做一定的限制")
    public Result<Void> checkTemplateValidForCreate(@PathVariable("logicClusterId") Long logicClusterId,
                                                    @PathVariable("templateSize") String templateSize) {
        return Result.buildSucc();
    }
    
    @PutMapping()
    @ResponseBody
    @ApiOperation(value = "编辑逻辑集群接口")
    public Result<Void> modifyLogicCluster(HttpServletRequest request, @RequestBody ESLogicClusterDTO param) {
        return clusterLogicManager.editLogicCluster(param, HttpRequestUtils.getOperator(request),
                HttpRequestUtils.getAppId(request));
    }
    
    @DeleteMapping("{clusterId}")
    @ResponseBody
    @ApiOperation(value = "下线集群")
    
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long clusterId) throws AdminOperateException {
        return clusterLogicManager.deleteLogicCluster(clusterId, HttpRequestUtils.getOperator(request),
                HttpRequestUtils.getAppId(request));
    }
    
    @GetMapping("/index-template-count/{clusterId}")
    @ResponseBody
    @ApiOperation(value = "提示用户索引和模板的数量")
    public Result<ClusterLogicTemplateIndexCountVO> indexTemplateCount(HttpServletRequest request, @PathVariable Long clusterId) {
        return clusterLogicManager.indexTemplateCount(clusterId, HttpRequestUtils.getOperator(request),
                HttpRequestUtils.getAppId(request));
    }
    
    @GetMapping("/nodes/{clusterLogicId}")
    @ResponseBody
    @ApiOperation(value = "获取指定逻辑集群列表接口")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true) })
    
    public PaginationResult<ESClusterRoleHostVO>  pageGetESClusterRoleHostVO(@PathVariable Long clusterLogicId,@RequestBody ClusterLogicNodeConditionDTO condition) {
        List<ESClusterRoleHostVO>  nodes = clusterNodeManager.convertClusterLogicNodes(clusterLogicNodeService.getLogicClusterNodesIncludeNonDataNodes(clusterLogicId));
        return clusterLogicManager.nodesPage(nodes,condition);
    }
}