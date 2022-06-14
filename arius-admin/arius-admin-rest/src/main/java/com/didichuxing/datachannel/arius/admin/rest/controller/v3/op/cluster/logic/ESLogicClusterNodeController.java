package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

/**
 * ES逻辑集群节点接口.
 *
 * @ClassName ESLogicClusterNodeController
 * @Author gyp
 * @Date 2022/6/13
 * @Version 1.0
 */
@RestController
@RequestMapping({ V3 + "/cluster/logic/node" })
@Api(tags = "ES逻辑集群节点接口(REST)")
public class ESLogicClusterNodeController {
    @Autowired
    private ClusterNodeManager clusterNodeManager;

    @GetMapping("/{clusterId}")
    @ResponseBody
    @ApiOperation(value = "获取节点列表")
    public Result<List<ESClusterRoleHostVO>> getClusterLogicNodes(@PathVariable Integer clusterId) {
        return clusterNodeManager.listClusterLogicNode(clusterId);
    }
}