package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

/**
 * @author wangshu
 * @date 2020/10/10
 */
@RestController
@RequestMapping(V3 + "/cluster/logic/region")
@Api(tags = "ES逻辑集群region接口(REST)")
public class ESLogicClusterRegionController {

    @Autowired
    private ClusterRegionService clusterRegionService;

    @Autowired
    private ClusterRegionManager clusterRegionManager;

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "查询逻辑集群region列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "logicClusterId", value = "logicClusterId", required = true) })
    public Result<List<ClusterRegionVO>> listLogicClusterRegions(@RequestParam("logicClusterId") Long logicClusterId) {

        List<ClusterRegion> regions = clusterRegionService.listLogicClusterRegions(logicClusterId);
        return Result.buildSucc(clusterRegionManager.buildLogicClusterRegionVO(regions));
    }
}