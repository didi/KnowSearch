package com.didichuxing.datachannel.arius.admin.biz.gateway.impl;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayJoinLogManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayJoinVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.GatewayJoin;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.metadata.service.GatewayJoinLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class GatewayJoinLogManagerImpl implements GatewayJoinLogManager {

    @Autowired
    private GatewayJoinLogService gatewayJoinLogService;

    @Override
    public Result<List<GatewayJoinVO>> getGatewayErrorList(Long projectId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                gatewayJoinLogService.getGatewayErrorList(projectId, startDate, endDate).getData(),
                GatewayJoinVO.class));
    }

    @Override
    public Result<List<GatewayJoinVO>> getGatewaySlowList(Long projectId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                gatewayJoinLogService.getGatewaySlowList(projectId, startDate, endDate).getData(),
                GatewayJoinVO.class));
    }

    @Override
    public Result<Long> getSearchCountByProjectId(String dataCenter, Long projectId, Long startDate,
                                                  Long endDate) {
        return gatewayJoinLogService.getSearchCountByProjectId(projectId, startDate, endDate);
    }

    @Override
    public Result<List<GatewayJoinVO>> getGatewayJoinSlowList(Integer projectId, GatewayJoinQueryDTO queryDTO) {
        List<GatewayJoin> gatewayJoinList = gatewayJoinLogService.getGatewaySlowList(projectId, queryDTO);
        if (CollectionUtils.isEmpty(gatewayJoinList)) {
            return Result.buildSucc(new ArrayList<>());
        }
        List<GatewayJoinVO> gatewayJoinVOList = ConvertUtil.list2List(gatewayJoinList, GatewayJoinVO.class);
        return Result.buildSucc(gatewayJoinVOList);
    }

    @Override
    public Result<List<GatewayJoinVO>> getGatewayJoinErrorList(Integer projectId, GatewayJoinQueryDTO queryDTO) {
        List<GatewayJoin> gatewayJoinList = gatewayJoinLogService.getGatewayJoinErrorList(projectId, queryDTO);
        if (CollectionUtils.isEmpty(gatewayJoinList)) {
            return Result.buildSucc(new ArrayList<>());
        }
        List<GatewayJoinVO> gatewayJoinVOList = ConvertUtil.list2List(gatewayJoinList, GatewayJoinVO.class);
        return Result.buildSucc(gatewayJoinVOList);
    }
}