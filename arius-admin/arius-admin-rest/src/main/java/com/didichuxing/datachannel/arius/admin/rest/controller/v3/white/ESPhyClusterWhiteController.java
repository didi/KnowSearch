package com.didichuxing.datachannel.arius.admin.rest.controller.v3.white;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_WHITE_PART;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.zeus.ZeusCollectManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESZeusHostInfoDTO;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Created by linyunan on 2021-06-25
 */
@RestController
@RequestMapping(V3_WHITE_PART + "/phy/cluster")
@Api(tags = "物理集群白名单接口")
public class ESPhyClusterWhiteController {

    @Autowired
    private ClusterPhyManager  clusterPhyManager;

    @Autowired
    private ZeusCollectManager zeusCollectManager;

    @DeleteMapping("{clusterId}/deleteClusterJoin")
    @ResponseBody
    @ApiOperation(value = "删除接入集群")
    public Result<Void> deleteClusterJoin(HttpServletRequest request, @PathVariable Integer clusterId) {
        return clusterPhyManager.deleteClusterJoin(clusterId, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/updateHttpAddress")
    @ResponseBody
    @ApiOperation(value = "更新物理集群http读写地址", notes = "用于zeus新建操作, 由启动脚本访问此接口")
    public Result<Boolean> updateHttpAddress(@RequestBody ESZeusHostInfoDTO esZeusHostInfoDTO) {
        return zeusCollectManager.updateHttpAddressFromZeus(esZeusHostInfoDTO);
    }

    @GetMapping("{clusterPhyName}/checkHealth")
    @ResponseBody
    @ApiOperation(value = "检查集群状态")
    public Result<Boolean> checkClusterHealth(HttpServletRequest request, @PathVariable String clusterPhyName) {
        return clusterPhyManager.checkClusterHealth(clusterPhyName, HttpRequestUtil.getOperator(request));
    }

    @GetMapping("{clusterPhyName}/isExit")
    @ResponseBody
    @ApiOperation(value = "集群是否存在")
    public Result<Boolean> checkClusterIsExit(HttpServletRequest request, @PathVariable String clusterPhyName) {
        return clusterPhyManager.checkClusterIsExit(clusterPhyName, HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("{clusterPhyName}/del")
    @ResponseBody
    @ApiOperation(value = "删除存在的集群")
    public Result<Boolean> deleteClusterExit(HttpServletRequest request, @PathVariable String clusterPhyName) {
        return clusterPhyManager.deleteClusterExit(clusterPhyName, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request));
    }
}