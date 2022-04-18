package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.capacityplan.IndexPlanManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.expire.TemplateExpireManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.TemplatePreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusOpThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

@RestController
@RequestMapping({ V2_OP + "/cluster", V2_OP + "/phy/cluster" })
@Api(tags = "es物理集群集群接口(REST)")
public class ESPhyClusterController {

    @Autowired
    private ESClusterService         esClusterService;

    @Autowired
    private TemplatePreCreateManager templatePreCreateManager;

    @Autowired
    private TemplateExpireManager    templateExpireManager;

    @Autowired
    private IndexPlanManager indexPlanManager;

    @Autowired
    private RoleClusterHostService roleClusterHostService;

    @Autowired
    private ClusterNodeManager       clusterNodeManager;

    @Autowired
    private ClusterPhyManager        clusterPhyManager;

    @Autowired
    private AriusOpThreadPool        ariusOpThreadPool;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取集群列表接口")
    public Result<List<ConsoleClusterPhyVO>> list(@RequestBody ESClusterDTO param, HttpServletRequest request) {
        return Result.buildSucc(clusterPhyManager.getConsoleClusterPhyVOS(param));
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取单个集群详情接口")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "clusterId", value = "集群ID", required = true) })
    public Result<ConsoleClusterPhyVO> get(@RequestParam("clusterId") Integer clusterId, HttpServletRequest request) {
        return Result.buildSucc(clusterPhyManager.getConsoleClusterPhy(clusterId, HttpRequestUtils.getAppId(request)));
    }

    @DeleteMapping("/del")
    @ResponseBody
    @ApiOperation(value = "删除集群接口", notes = "删除集群相关所有信息")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "clusterId", value = "集群ID", required = true) })
    public Result<Boolean> delete(HttpServletRequest request, @RequestParam(value = "clusterId") Integer clusterId) {
        return clusterPhyManager.deleteClusterInfo(clusterId, HttpRequestUtils.getOperator(request), HttpRequestUtils.getAppId(request));
    }

    @PutMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建集群接口", notes = "")
    public Result<Boolean> add(HttpServletRequest request, @RequestBody ESClusterDTO param) {
        return clusterPhyManager.addCluster(param, HttpRequestUtils.getOperator(request), HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/edit")
    @ResponseBody
    @ApiOperation(value = "编辑集群接口", notes = "")
    public Result<Boolean> edit(HttpServletRequest request, @RequestBody ESClusterDTO param) {
        return clusterPhyManager.editCluster(param, HttpRequestUtils.getOperator(request),HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/collectClusterNodeSettings")
    @ResponseBody
    @ApiOperation(value = "采集集群节点配置信息接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<Void> collectClusterNodeSettings(@RequestParam(value = "cluster") String cluster) {
        return Result.build(roleClusterHostService.collectClusterNodeSettings(cluster));
    }

    @PostMapping("/closeRebalance")
    @ResponseBody
    @ApiOperation(value = "关闭集群rebalance接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<Void> closeRebalance(@RequestParam(value = "cluster") String cluster) throws AdminOperateException {
        return Result.build(esClusterService.syncCloseReBalance(cluster, 0));
    }

    @PostMapping("/adjustShardCount")
    @ResponseBody
    @ApiOperation(value = "调整shard个数", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<Void> adjustShardCount(@RequestParam(value = "cluster") String cluster) {
        return indexPlanManager.adjustShardCountByPhyClusterName(cluster);
    }

    @PostMapping("/node/list")
    @ResponseBody
    @ApiOperation(value = "获取集群节点列表接口", notes = "")
    public Result<List<ESRoleClusterHostVO>> nodeList(@RequestBody ESRoleClusterHostDTO param) {
        return Result
            .buildSucc(clusterNodeManager.convertClusterLogicNodes(roleClusterHostService.queryNodeByCondt(param)));
    }

    @GetMapping("/node/getByCluster")
    @ResponseBody
    @ApiOperation(value = "根据集群获取集群节点列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<List<ESRoleClusterHostVO>> getNodesByCluster(@RequestParam(value = "cluster") String cluster) {
        return Result
            .buildSucc(clusterNodeManager.convertClusterLogicNodes(roleClusterHostService.getNodesByCluster(cluster)));
    }

    @PostMapping("/node/status")
    @ResponseBody
    @ApiOperation(value = "修改节点状态", notes = "")
    public Result<Void> editNodeStatus(HttpServletRequest request, @RequestBody ESRoleClusterHostDTO param) {
        return roleClusterHostService.editNodeStatus(param, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/releaseRack")
    @ResponseBody
    @ApiOperation(value = "释放集群rack", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "String", name = "rack", value = "rack", required = true) })
    public Result<Void> releaseRack(HttpServletRequest request, @RequestParam(value = "cluster") String cluster,
                              @RequestParam(value = "rack") String racks) {
        return clusterPhyManager.releaseRacks(cluster, racks, 3);
    }

    @PostMapping("/deleteExpireIndex")
    @ResponseBody
    @ApiOperation(value = "删除过期索引接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<Void> deleteExpireIndex(@RequestParam(value = "cluster") String cluster) {
        ariusOpThreadPool.execute(() -> templateExpireManager.deleteExpireIndex(cluster));
        return Result.buildSucc();
    }

    @PostMapping("/preCreateIndex")
    @ResponseBody
    @ApiOperation(value = "索引预先创建接口", notes = "创建明天索引")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<Void> preCreateIndex(@RequestParam(value = "cluster") String cluster) {
        ariusOpThreadPool.execute(() -> templatePreCreateManager.preCreateIndex(cluster, 0));
        return Result.buildSucc();
    }

    // *************************************** RESTFUL  API ***************************************
    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取集群列表接口", notes = "")
    public Result<List<ConsoleClusterPhyVO>> getPhyClusters(@RequestParam ESClusterDTO param,
                                                            HttpServletRequest request) {
        return Result.buildSucc(clusterPhyManager.getConsoleClusterPhyVOS(param, HttpRequestUtils.getAppId(request)));
    }
}
