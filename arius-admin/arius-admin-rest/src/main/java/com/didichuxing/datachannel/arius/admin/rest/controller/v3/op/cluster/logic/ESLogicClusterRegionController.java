package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangshu
 * @date 2020/10/10
 */
@RestController
@RequestMapping(V3 + "/logic/cluster/region")
@Api(tags = "ES逻辑集群region接口(REST)")
public class ESLogicClusterRegionController {

    @Autowired
    private ClusterRegionService clusterRegionService;

    @Autowired
    private ClusterRegionManager clusterRegionManager;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "查询逻辑集群region列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "logicClusterId", value = "logicClusterId", required = true) })
    public Result<List<ClusterRegionVO>> listLogicClusterRegions(@RequestParam("logicClusterId") Long logicClusterId) {

        List<ClusterRegion> regions = clusterRegionService.listLogicClusterRegions(logicClusterId);
        return Result.buildSucc(clusterRegionManager.buildLogicClusterRegionVO(regions));
    }

    @DeleteMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "解绑逻辑集群region接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "regionId", value = "regionId", required = true) })
    public Result<Void> cancelBindingLogicClusterRegion(HttpServletRequest request,
                                                        @RequestParam("regionId") Long regionId,
                                                        @RequestParam("logicClusterId")Long logicClusterId) {

        return clusterRegionManager.unbindRegion(regionId, logicClusterId, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "绑定逻辑集群region接口", notes = "")
    public Result<Void> bindingLogicClusterRegion(HttpServletRequest request,
                                                  @RequestBody ESLogicClusterWithRegionDTO param) throws AdminOperateException {
        param.setProjectId(HttpRequestUtil.getProjectId(request));
        return clusterRegionManager.batchBindRegionToClusterLogic(param, HttpRequestUtil.getOperator(request),
            Boolean.FALSE);
    }
}