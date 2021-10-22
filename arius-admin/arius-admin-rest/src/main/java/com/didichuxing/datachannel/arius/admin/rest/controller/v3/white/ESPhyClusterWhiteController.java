package com.didichuxing.datachannel.arius.admin.rest.controller.v3.white;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_WHITE_PART;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

/**
 * Created by linyunan on 2021-06-25
 */
@RestController
@RequestMapping(V3_WHITE_PART + "/phy/cluster")
@Api(value = "物理集群白名单接口")
public class ESPhyClusterWhiteController {

    @Autowired
    private ClusterPhyManager clusterPhyManager;

    @DeleteMapping("{clusterId}/deleteClusterJoin")
    @ResponseBody
    @ApiOperation(value = "删除接入集群")
    public Result deleteClusterJoin(HttpServletRequest request, @PathVariable Integer clusterId) {
        return clusterPhyManager.deleteClusterJoin(clusterId, HttpRequestUtils.getOperator(request));
    }
}
