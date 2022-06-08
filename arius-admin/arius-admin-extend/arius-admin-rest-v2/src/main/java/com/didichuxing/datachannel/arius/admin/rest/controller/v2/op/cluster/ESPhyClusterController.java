package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.expire.TemplateExpireManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.TemplatePreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusOpThreadPool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ohushenglin_v
 * @date 2022-05-23
 */
@RestController
@RequestMapping({ V2_OP + "/cluster"})
@Api(tags = "es物理集群集群接口(REST)")
public class ESPhyClusterController {

    @Autowired
    private TemplatePreCreateManager templatePreCreateManager;

    @Autowired
    private TemplateExpireManager    templateExpireManager;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private ClusterNodeManager       clusterNodeManager;

    @Autowired
    private ClusterPhyManager        clusterPhyManager;

    @Autowired
    private AriusOpThreadPool        ariusOpThreadPool;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取集群列表接口【三方接口】",tags = "【三方接口】")
    public Result<List<ClusterPhyVO>> list(@RequestBody ClusterPhyDTO param, HttpServletRequest request) {
        return Result.buildSucc(clusterPhyManager.getClusterPhys(param));
    }

    @PutMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建集群接口【三方接口】",tags = "【三方接口】" )

    public Result<Boolean> add(HttpServletRequest request, @RequestBody ClusterPhyDTO param) {
        return clusterPhyManager.addCluster(param, HttpRequestUtil.getOperator(request), HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/edit")
    @ResponseBody
    @ApiOperation(value = "编辑集群接口" )
    @Deprecated
    public Result<Boolean> edit(HttpServletRequest request, @RequestBody ClusterPhyDTO param) {
        return clusterPhyManager.editCluster(param, HttpRequestUtil.getOperator(request));
    }

    @PostMapping("/collectClusterNodeSettings")
    @ResponseBody
    @ApiOperation(value = "采集集群节点配置信息接口【三方接口】",tags = "【三方接口】" )

    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<Void> collectClusterNodeSettings(@RequestParam(value = "cluster") String cluster) {
        return Result.build(clusterRoleHostService.collectClusterNodeSettings(cluster));
    }

    @PostMapping("/node/list")
    @ResponseBody
    @ApiOperation(value = "获取集群节点列表接口【三方接口】",tags = "【三方接口】" )
    @Deprecated
    public Result<List<ESClusterRoleHostVO>> nodeList(@RequestBody ESClusterRoleHostDTO param) {
       return Result.buildSucc();
    }

    @GetMapping("/node/getByCluster")
    @ResponseBody
    @ApiOperation(value = "根据集群获取集群节点列表接口【三方接口】",tags = "【三方接口】")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    @Deprecated
    public Result<List<ESClusterRoleHostVO>> getNodesByCluster(@RequestParam(value = "cluster") String cluster) {
        return Result.buildSucc();
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