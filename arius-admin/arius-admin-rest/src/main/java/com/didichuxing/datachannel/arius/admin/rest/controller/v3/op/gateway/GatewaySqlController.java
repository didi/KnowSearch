package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.gateway;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@RestController()
@RequestMapping(V3 + "/gateway/sql")
@Api(tags = "Gateway中sql语句的翻译和查询")
public class GatewaySqlController {
    @Autowired
    private GatewayManager gatewayManager;

    @PostMapping(value = { "/{phyClusterName}"})
    @ResponseBody
    @ApiOperation(value = "根据sql语句查询gateway集群")
    public Result<String> directSqlSearch(@RequestBody String sql,
                                          @PathVariable("phyClusterName") String phyClusterName,
                                          HttpServletRequest request) {
        return gatewayManager.directSqlSearch(sql, phyClusterName, HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/{phyClusterName}/explain")
    @ResponseBody
    @ApiOperation(value = "根据sql语句解释")
    public Result<String> sqlExplain(@RequestBody String sql ,@PathVariable("phyClusterName") String phyClusterName) {
        return gatewayManager.sqlExplain(sql, phyClusterName,AuthConstant.SUPER_PROJECT_ID);
    }
}