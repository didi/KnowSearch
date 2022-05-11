package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.expire.TemplateExpireManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.TemplatePreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusOpThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterHostService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping({ V2_OP + "/cluster"})
@Api(tags = "es物理集群集群接口(REST)")
public class ESPhyClusterController {

    @Autowired
    private TemplatePreCreateManager templatePreCreateManager;

    @Autowired
    private TemplateExpireManager    templateExpireManager;

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
    public Result<List<ConsoleClusterPhyVO>> list(@RequestBody ClusterPhyDTO param, HttpServletRequest request) {
        return Result.buildSucc(clusterPhyManager.getConsoleClusterPhyVOS(param));
    }

    @PutMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建集群接口" )

    public Result<Boolean> add(HttpServletRequest request, @RequestBody ClusterPhyDTO param) {
        return clusterPhyManager.addCluster(param, HttpRequestUtils.getOperator(request), HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/edit")
    @ResponseBody
    @ApiOperation(value = "编辑集群接口" )

    public Result<Boolean> edit(HttpServletRequest request, @RequestBody ClusterPhyDTO param) {
        return clusterPhyManager.editCluster(param, HttpRequestUtils.getOperator(request),HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/collectClusterNodeSettings")
    @ResponseBody
    @ApiOperation(value = "采集集群节点配置信息接口" )

    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<Void> collectClusterNodeSettings(@RequestParam(value = "cluster") String cluster) {
        return Result.build(roleClusterHostService.collectClusterNodeSettings(cluster));
    }

    @PostMapping("/node/list")
    @ResponseBody
    @ApiOperation(value = "获取集群节点列表接口" )

    public Result<List<ESRoleClusterHostVO>> nodeList(@RequestBody ESRoleClusterHostDTO param) {
        return Result
            .buildSucc(clusterNodeManager.convertClusterLogicNodes(roleClusterHostService.queryNodeByCondt(param)));
    }

    @GetMapping("/node/getByCluster")
    @ResponseBody
    @ApiOperation(value = "根据集群获取集群节点列表接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })

    public Result<List<ESRoleClusterHostVO>> getNodesByCluster(@RequestParam(value = "cluster") String cluster) {
        return Result
            .buildSucc(clusterNodeManager.convertClusterLogicNodes(roleClusterHostService.getNodesByCluster(cluster)));
    }

    @PostMapping("/deleteExpireIndex")
    @ResponseBody
    @ApiOperation(value = "删除过期索引接口" )
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
}
