package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.GatewayJoin;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayJoinESDAO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GatewayJoinLogService {

    @Autowired
    private GatewayJoinESDAO gatewayJoinESDAO;

    

    public Result<Long> getSearchCountByProjectId(Long projectId, Long startDate, Long endDate) {
        return Result.buildSucc(gatewayJoinESDAO.getSearchCountByProjectId(projectId, startDate, endDate));
    }

    public Result<List<GatewayJoin>> getGatewayErrorList(Long projectId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil
            .list2List(gatewayJoinESDAO.getGatewayErrorList(projectId, startDate, endDate), GatewayJoin.class));
    }

    public Result<List<GatewayJoin>> getGatewaySlowList(Long projectId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil
            .list2List(gatewayJoinESDAO.getGatewaySlowList(projectId, startDate, endDate), GatewayJoin.class));
    }

    public List<GatewayJoin> getGatewaySlowList(Integer projectId, GatewayJoinQueryDTO queryDTO, Integer slowQueryTime) {
        initGatewaySlowQueryCondition(queryDTO, slowQueryTime);
        return ConvertUtil.list2List(gatewayJoinESDAO.getGatewayJoinSlowList(projectId, queryDTO), GatewayJoin.class);
    }

    public List<GatewayJoin> getGatewayJoinErrorList(Integer projectId, GatewayJoinQueryDTO queryDTO) {
        initQueryTimeRange(queryDTO);
        return ConvertUtil.list2List(gatewayJoinESDAO.getGatewayJoinErrorList(projectId, queryDTO), GatewayJoin.class);
    }

    private void initGatewaySlowQueryCondition(GatewayJoinQueryDTO queryDTO, int defaultSlowTime) {
        initQueryTimeRange(queryDTO);
        if (queryDTO.getTotalCost() == null) {
            queryDTO.setTotalCost((long) defaultSlowTime);
        }
    }

    private void initQueryTimeRange(GatewayJoinQueryDTO queryDTO) {
        if (queryDTO.getStartTime() == null) {
            long curTimestamp = System.currentTimeMillis();
            queryDTO.setStartTime(curTimestamp - AdminConstant.MILLIS_PER_DAY);
            queryDTO.setEndTime(curTimestamp);
        }
        if (queryDTO.getEndTime() == null) {
            queryDTO.setEndTime(System.currentTimeMillis());
        }
    }
    
    public String getOneDSLByProjectIdAndIndexName(Integer projectId, String indexName) {
        return gatewayJoinESDAO.getOneDSLByProjectIdAndIndexName(projectId, indexName);
    }
}