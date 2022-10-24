package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

/**
 * ES逻辑集群索引接口.
 *
 * @ClassName ESLogicClusterNodeController
 * @Author gyp
 * @Date 2022/6/13
 * @Version 1.0
 */
@RestController
@RequestMapping({ V3 + "/cluster/logic/index" })
@Api(tags = "ES逻辑集群索引接口(REST)")
public class ESLogicClusterIndexController {
    @Autowired
    private ClusterNodeManager clusterNodeManager;
}