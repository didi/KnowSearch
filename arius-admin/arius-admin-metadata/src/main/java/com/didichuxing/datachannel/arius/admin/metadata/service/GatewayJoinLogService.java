package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.GatewayJoin;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayJoinESDAO;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GatewayJoinLogService {

    @Autowired
    private GatewayJoinESDAO gatewayJoinESDAO;

    public Result<Long> getSearchCountByAppid(Long appId, Long startDate, Long endDate){
        return Result.buildSucc(gatewayJoinESDAO.getSearchCountByAppid(appId, startDate, endDate));
    }

    public Result<List<GatewayJoin>> getGatewayErrorList(Long appId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                gatewayJoinESDAO.getGatewayErrorList(appId, startDate, endDate),
                GatewayJoin.class));
    }

    public Result<List<GatewayJoin>> getGatewaySlowList(Long appId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                gatewayJoinESDAO.getGatewaySlowList(appId, startDate, endDate),
                GatewayJoin.class));
    }

    public List<GatewayJoin> getGatewaySlowList(Integer appId, GatewayJoinQueryDTO queryDTO) {
        initGatewaySlowQueryCondition(queryDTO);
        return ConvertUtil.list2List(gatewayJoinESDAO.getGatewayJoinSlowList(appId, queryDTO), GatewayJoin.class);
    }

    public List<GatewayJoin> getGatewayJoinErrorList(Integer appId, GatewayJoinQueryDTO queryDTO) {
        initQueryTimeRange(queryDTO);
        return ConvertUtil.list2List(gatewayJoinESDAO.getGatewayJoinErrorList(appId, queryDTO), GatewayJoin.class);
    }

    private void initGatewaySlowQueryCondition(GatewayJoinQueryDTO queryDTO) {
        initQueryTimeRange(queryDTO);
        if(queryDTO.getTotalCost() == null) {
            queryDTO.setTotalCost(1000L);
        }
    }

    private void initQueryTimeRange(GatewayJoinQueryDTO queryDTO) {
        if(queryDTO.getStartTime() == null) {
            long curTimestamp = System.currentTimeMillis();
            queryDTO.setStartTime(curTimestamp - AdminConstant.MILLIS_PER_DAY);
            queryDTO.setEndTime(curTimestamp);
        }
        if(queryDTO.getEndTime() == null) {
            queryDTO.setEndTime(System.currentTimeMillis());
        }
    }
}
