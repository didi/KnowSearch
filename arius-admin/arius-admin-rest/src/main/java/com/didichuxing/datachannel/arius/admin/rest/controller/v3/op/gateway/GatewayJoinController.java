package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.gateway;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayJoinLogManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayJoinVO;
import io.swagger.annotations.*;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

@NoArgsConstructor
@RestController()
@RequestMapping(V3_OP + "/gateway/join")
@Api(tags = "GatewayJoin日志接口")
public class GatewayJoinController {

    @Autowired
    private GatewayJoinLogManager gatewayJoinManager;

    @GetMapping(path = "/error/listByProjectId.do")
    @ApiOperation(value = "获取appid的错误gateway请求日志", notes = "获取projectId的错误gateway请求日志", httpMethod = "GET")
    public Result<List<GatewayJoinVO>> getGatewayErrorList(@ApiParam(name = "projectId", value = "应用账号", required = false, example = "1") @RequestParam(value = "projectId", required = false) Long projectId,
                                                           @ApiParam(name = "dataCenter", value = "数据中心", required = true, example = "cn") @RequestParam(value = "dataCenter") String dataCenter,
                                                           @ApiParam(name = "startDate", value = "开始时刻", required = true, example = "1550160000000") @RequestParam(value = "startDate") Long startDate,
                                                           @ApiParam(name = "endDate", value = "结束时刻", required = true, example = "1550246399999") @RequestParam(value = "endDate") Long endDate) {
        return gatewayJoinManager.getGatewayErrorList(projectId, startDate, endDate);
    }

    @GetMapping(path = "/slow/listByProjectId.do")
    @ApiOperation(value = "获取appid的慢查gateway请求日志", notes = "获取projectId的慢查gateway请求日志", httpMethod = "GET")
    public Result<List<GatewayJoinVO>> getGatewaySlowList(@ApiParam(name = "projectId", value = "应用账号", required = false, example = "1") @RequestParam(value = "projectId", required = false) Long projectId,
                                                          @ApiParam(name = "dataCenter", value = "数据中心", required = true, example = "cn") @RequestParam(value = "dataCenter") String dataCenter,
                                                          @ApiParam(name = "startDate", value = "开始时刻", required = true, example = "1550160000000") @RequestParam(value = "startDate") Long startDate,
                                                          @ApiParam(name = "endDate", value = "结束时刻", required = true, example = "1550246399999") @RequestParam(value = "endDate") Long endDate) {
        return gatewayJoinManager.getGatewaySlowList(projectId, startDate, endDate);
    }

    @GetMapping(path = "/query/countByProjectId.do")
    @ApiOperation(value = "获取appid的查询次数", notes = "获取projectId的查询次数", httpMethod = "GET")
    public Result<Long> getSearchCountByAppid(@ApiParam(name = "projectId", value = "应用账号", required = false, example = "1") @RequestParam(value = "projectId", required = true) Long projectId,
                                              @ApiParam(name = "dataCenter", value = "数据中心", required = true, example = "cn") @RequestParam(value = "dataCenter") String dataCenter,
                                              @ApiParam(name = "startDate", value = "开始时刻", required = true, example = "1550160000000") @RequestParam(value = "startDate") Long startDate,
                                              @ApiParam(name = "endDate", value = "结束时刻", required = true, example = "1550246399999") @RequestParam(value = "endDate") Long endDate) {
        return gatewayJoinManager.getSearchCountByAppId(dataCenter, projectId, startDate, endDate);
    }
}