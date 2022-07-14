package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

@RestController
@RequestMapping(V3 + "/logic/cluster/templateSrv")
@Api(tags = "ES逻辑集群索引服务接口(REST)")
@Deprecated
public class ClusterLogicTemplateSrvController {

    @Autowired
    private TemplateSrvManager templateSrvManager;

}