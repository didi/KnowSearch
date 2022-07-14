package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.gateway;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import io.swagger.annotations.Api;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

@NoArgsConstructor
@RestController()
@RequestMapping(V3 + "/gateway/sql")
@Api(tags = "Gateway中sql语句的翻译和查询")
public class GatewaySqlController {
    @Autowired
    private GatewayManager gatewayManager;
}