package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;

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

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.expire.TemplateExpireManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.TemplatePreCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.shard.TemplateShardManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhyDiscover;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusOpThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 物理集群Controller
 * @author d06679
 * @date 2017/10/23
 */
@RestController
@RequestMapping({ V2_OP + "/cluster", V2_OP + "/phy/cluster" })
@Api(value = "es物理集群集群接口(REST)")
public class ESPhyClusterController {

    @Autowired
    private ESClusterPhyService      esClusterPhyService;

    @Autowired
    private ESClusterService         esClusterService;

    @Autowired
    private TemplatePreCreateManager templatePreCreateManager;

    @Autowired
    private TemplateExpireManager    templateExpireManager;

    @Autowired
    private TemplateShardManager     templateShardManager;

    @Autowired
    private ESRoleClusterHostService esRoleClusterHostService;

    @Autowired
    private ClusterNodeManager       clusterNodeManager;

    @Autowired
    private ClusterPhyManager        clusterPhyManager;

    @Autowired
    private AriusOpThreadPool        ariusOpThreadPool;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取集群列表接口")
    @Deprecated
    public Result<List<ConsoleClusterPhyVO>> list(@RequestBody ESClusterDTO param, HttpServletRequest request) {
        return Result.buildSucc(clusterPhyManager.getConsoleClusterPhyVOS(param, HttpRequestUtils.getAppId(request)));
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取单个集群详情接口")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "clusterId", value = "集群ID", required = true) })
    public Result<ConsoleClusterPhyVO> get(@RequestParam("clusterId") Integer clusterId, HttpServletRequest request) {
        return Result.buildSucc(clusterPhyManager.getConsoleClusterPhyVO(clusterId, HttpRequestUtils.getAppId(request)));
    }

    @DeleteMapping("/del")
    @ResponseBody
    @ApiOperation(value = "删除集群接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "clusterId", value = "集群ID", required = true) })
    public Result delete(HttpServletRequest request, @RequestParam(value = "clusterId") Integer clusterId) {
        return esClusterPhyService.deleteClusterById(clusterId, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建集群接口", notes = "")
    public Result add(HttpServletRequest request, @RequestBody ESClusterDTO param) {
        //TODO: lyn, associate with appId
        Integer appId = HttpRequestUtils.getAppId(request);
        return esClusterPhyService.createCluster(param, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/edit")
    @ResponseBody
    @ApiOperation(value = "编辑集群接口", notes = "")
    public Result edit(HttpServletRequest request, @RequestBody ESClusterDTO param) {
        return esClusterPhyService.editCluster(param, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/collectClusterNodeSettings")
    @ResponseBody
    @ApiOperation(value = "采集集群节点配置信息接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result collectClusterNodeSettings(@RequestParam(value = "cluster") String cluster) {
        return Result.build(esRoleClusterHostService.collectClusterNodeSettings(cluster));
    }

    @PostMapping("/closeRebalance")
    @ResponseBody
    @ApiOperation(value = "关闭集群rebalance接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result closeRebalance(@RequestParam(value = "cluster") String cluster) throws AdminOperateException {
        return Result.build(esClusterService.syncCloseReBalance(cluster, 0));
    }

    @PostMapping("/adjustShardCount")
    @ResponseBody
    @ApiOperation(value = "调整shard个数", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result adjustShardCount(@RequestParam(value = "cluster") String cluster) throws AdminOperateException {
        return Result.build(templateShardManager.adjustShardCount(cluster, 0));
    }

    @PostMapping("/node/list")
    @ResponseBody
    @ApiOperation(value = "获取集群节点列表接口", notes = "")
    public Result<List<ESRoleClusterHostVO>> nodeList(@RequestBody ESRoleClusterHostDTO param) {
        return Result
            .buildSucc(clusterNodeManager.convertClusterNodes(esRoleClusterHostService.queryNodeByCondt(param)));
    }

    @GetMapping("/node/getByCluster")
    @ResponseBody
    @ApiOperation(value = "根据集群获取集群节点列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<List<ESRoleClusterHostVO>> getNodesByCluster(@RequestParam(value = "cluster") String cluster) {
        return Result
            .buildSucc(clusterNodeManager.convertClusterNodes(esRoleClusterHostService.getNodesByCluster(cluster)));
    }

    @PostMapping("/node/status")
    @ResponseBody
    @ApiOperation(value = "修改节点状态", notes = "")
    public Result editNodeStatus(HttpServletRequest request, @RequestBody ESRoleClusterHostDTO param) {
        return esRoleClusterHostService.editNodeStatus(param, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/releaseRack")
    @ResponseBody
    @ApiOperation(value = "释放集群rack", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "String", name = "rack", value = "rack", required = true) })
    public Result releaseRack(HttpServletRequest request, @RequestParam(value = "cluster") String cluster,
                              @RequestParam(value = "rack") String racks) {
        return clusterPhyManager.releaseRacks(cluster, racks, 3);
    }

    @GetMapping("/plugins")
    @ResponseBody
    @ApiOperation(value = "获取运维侧物理集群所有安装插件", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    @Deprecated
    public Result getClusterPlugins(@RequestParam(value = "cluster") String cluster) {
        return Result.buildSucc(esClusterPhyService.listClusterPlugins(cluster));
    }

    @GetMapping("/discover")
    @ResponseBody
    @ApiOperation(value = "获取当前集群Discover列表", notes = "获取集群监控日志访问方式")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<List<ESClusterPhyDiscover>> getClusterDiscovers(HttpServletRequest request,
                                                                  @RequestParam(value = "cluster") String cluster) {
        return Result.buildSucc(esClusterPhyService.getClusterDiscovers(cluster));
    }

    @PostMapping("/deleteExpireIndex")
    @ResponseBody
    @ApiOperation(value = "删除过期索引接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result deleteExpireIndex(@RequestParam(value = "cluster") String cluster) throws AdminOperateException {
        ariusOpThreadPool.execute(() -> templateExpireManager.deleteExpireIndex(cluster));
        return Result.buildSucc();
    }

    @PostMapping("/preCreateIndex")
    @ResponseBody
    @ApiOperation(value = "索引预先创建接口", notes = "创建明天索引")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result preCreateIndex(@RequestParam(value = "cluster") String cluster) throws AdminOperateException {
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
