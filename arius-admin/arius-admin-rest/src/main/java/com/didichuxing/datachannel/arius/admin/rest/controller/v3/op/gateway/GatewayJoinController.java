package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.gateway;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayJoinLogManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.GatewayJoinVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

@NoArgsConstructor
@RestController()
@RequestMapping(V3_OP + "/gateway/join")
@Api(tags = "gatewayJoin日志接口")
public class GatewayJoinController {

    @Autowired
    private GatewayJoinLogManager gatewayJoinManager;

    /**
     * 获取appid的错误gateway请求日志
     * @return
     */
    @GetMapping(path = "/error/listByAppid.do")
    @ApiOperation(value = "获取appid的错误gateway请求日志", notes = "获取appid的错误gateway请求日志", httpMethod = "GET")
    public Result<List<GatewayJoinVO>> getGatewayErrorList(@ApiParam(name="appId", value="应用账号", required = false, example = "1")
                                                           @RequestParam(value = "appId", required = false) Long appId,

                                                           @ApiParam(name="dataCenter", value="数据中心", required = true, example = "cn")
                                                           @RequestParam(value = "dataCenter") String dataCenter,

                                                           @ApiParam(name="startDate", value="开始时刻", required = true, example = "1550160000000")
                                                           @RequestParam(value = "startDate")  Long startDate,

                                                           @ApiParam(name="endDate", value="结束时刻", required = true, example = "1550246399999")
                                                           @RequestParam(value = "endDate") Long endDate){
        return gatewayJoinManager.getGatewayErrorList(appId, startDate, endDate);
    }

    /**
     * 获取appid的慢查gateway请求日志
     * @return
     */
    @GetMapping(path = "/slow/listByAppid.do")
    @ApiOperation(value = "获取appid的慢查gateway请求日志", notes = "获取appid的慢查gateway请求日志", httpMethod = "GET")
    public Result<List<GatewayJoinVO>> getGatewaySlowList(@ApiParam(name="appId", value="应用账号", required = false, example = "1")
                                                          @RequestParam(value = "appId", required = false) Long appId,

                                                          @ApiParam(name="dataCenter", value="数据中心", required = true, example = "cn")
                                                          @RequestParam(value = "dataCenter") String dataCenter,

                                                          @ApiParam(name="startDate", value="开始时刻", required = true, example = "1550160000000")
                                                          @RequestParam(value = "startDate") Long startDate,

                                                          @ApiParam(name="endDate", value="结束时刻", required = true, example = "1550246399999")
                                                          @RequestParam(value = "endDate")  Long endDate){
        return gatewayJoinManager.getGatewaySlowList(appId, startDate, endDate);
    }
}
