package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.gateway;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.DEFAULT_APP_ID;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

@NoArgsConstructor
@RestController()
@RequestMapping(V3_OP + "/gateway/sql")
@Api(tags = "Gateway中sql语句的翻译和查询")
public class GatewaySqlController {
    @Autowired
    private GatewayManager gatewayManager;

    @PostMapping(value = {"/{phyClusterName}", ""})
    @ResponseBody
    @ApiOperation(value = "根据sql语句查询gateway集群")
    public Result<String> directSqlSearch(@RequestBody String sql,
                                          @PathVariable(required = false) String phyClusterName,
                                          HttpServletRequest request) {
        return gatewayManager.directSqlSearch(sql, phyClusterName, HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/explain")
    @ResponseBody
    @ApiOperation(value = "根据sql语句解释")
    public Result<String> sqlExplain(@RequestBody String sql) {
        return gatewayManager.sqlExplain(sql, DEFAULT_APP_ID);
    }
}
