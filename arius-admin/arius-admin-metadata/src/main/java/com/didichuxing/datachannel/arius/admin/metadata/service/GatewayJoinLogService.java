package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.GatewayJoin;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayJoinPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
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
    
    /**
     *  通过项目 id 和索引名称获取一个 DSL
     *
     * @param projectId 您要查询的项目的项目ID。
     * @param indexName 索引的名称
     * @return String
     */
    public String getOneDSLByProjectIdAndIndexName(Integer projectId, String indexName) {
        return gatewayJoinESDAO.getOneDSLByProjectIdAndIndexName(projectId, indexName);
    }

    /**
     * 分页查询gatewayJoin慢查询日志
     * @param projectId
     * @param queryDTO
     * @param slowQueryTime
     * @return
     */
    public Tuple<Long, List<GatewayJoinPO>> getGatewayJoinSlowQueryLogPage(Integer projectId, GatewayJoinQueryDTO queryDTO, Integer slowQueryTime) throws ESOperateException {
        initGatewaySlowQueryCondition(queryDTO, slowQueryTime);
        return gatewayJoinESDAO.getGatewayJoinSlowQueryPage(projectId, queryDTO);
    }

    /**
     * 分页查询gatewayJoin异常日志
     * @param projectId
     * @param queryDTO
     * @return
     */
    public Tuple<Long, List<GatewayJoinPO>> getGatewayJoinErrorLogPage(Integer projectId, GatewayJoinQueryDTO queryDTO) throws ESOperateException {
        initQueryTimeRange(queryDTO);
        return gatewayJoinESDAO.getGatewayJoinErrorPage(projectId, queryDTO);
    }
}