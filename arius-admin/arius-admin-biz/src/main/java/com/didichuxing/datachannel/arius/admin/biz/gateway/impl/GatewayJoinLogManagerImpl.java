package com.didichuxing.datachannel.arius.admin.biz.gateway.impl;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayJoinLogManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.GatewayJoinVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.metadata.service.GatewayJoinLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GatewayJoinLogManagerImpl implements GatewayJoinLogManager {

    @Autowired
    private GatewayJoinLogService gatewayJoinLogService;

    @Override
    public Result<List<GatewayJoinVO>> getGatewayErrorList(Long appId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                gatewayJoinLogService.getGatewayErrorList(appId, startDate, endDate).getData(),
                GatewayJoinVO.class));
    }

    @Override
    public Result<List<GatewayJoinVO>> getGatewaySlowList(Long appId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                gatewayJoinLogService.getGatewaySlowList(appId, startDate, endDate).getData(),
                GatewayJoinVO.class));
    }
}
