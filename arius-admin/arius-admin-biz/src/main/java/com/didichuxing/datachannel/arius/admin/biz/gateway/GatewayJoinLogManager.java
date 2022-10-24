package com.didichuxing.datachannel.arius.admin.biz.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayJoinVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

import java.util.List;

public interface GatewayJoinLogManager {
    /**
     * 获取projectId的错误gateway请求日志
     * @param projectId 应用账号
     * @param startDate 开始时刻
     * @param endDate 结束时刻
     * @return list
     */
    Result<List<GatewayJoinVO>> getGatewayErrorList(Long projectId, Long startDate, Long endDate);

    /**
     * 获取projectId的慢查gateway请求日志
     * @param projectId 应用账号
     * @param startDate 开始时刻
     * @param endDate 结束时刻
     * @return list
     */
    Result<List<GatewayJoinVO>> getGatewaySlowList(Long projectId, Long startDate, Long endDate);

    /**
     * 根据projectId获取指定数据中心一段时间查询量
     *
     * @param dataCenter
     * @param projectId
     * @param startDate
     * @param endDate
     * @return
     */
    Result<Long> getSearchCountByProjectId(String dataCenter, Long projectId, Long startDate, Long endDate);

    /**
     * 获取指定索引的DSL
     *
     * @param projectId 项目编号
     * @param indexName 索引的名称
     * @return Result<String>
     */
    Result<String> getDSLByProjectIdAndIndexName(Integer projectId, String indexName);

    /**
     * 分页查询
     * @param projectId
     * @param queryDTO
     * @return
     */
    PaginationResult<GatewayJoinVO> getGatewayJoinPage(Integer projectId, GatewayJoinQueryDTO queryDTO) throws NotFindSubclassException;
}