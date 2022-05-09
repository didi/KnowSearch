package com.didichuxing.datachannel.arius.admin.rest.controller.v2.thirdpart;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_THIRD_PART;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.thardpart.CommonManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartClusterVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V2_THIRD_PART + "/common")
@Api(tags = "第三方公共接口(REST)")
public class ThirdpartCommonController {

    @Autowired
    private CommonManager commonManager;

    @GetMapping("/cluster/list")
    @ResponseBody
    @ApiOperation(value = "获取物理集群列表接口" )
    public Result<List<ThirdPartClusterVO>> listDataCluster() {
        return commonManager.listDataCluster();

    }

    @GetMapping("/cluster/getByName")
    @ResponseBody
    @ApiOperation(value = "获取集群接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<ThirdPartClusterVO> getDataCluster(@RequestParam("cluster") String cluster) {
        return commonManager.getDataCluster(cluster);

    }
}
