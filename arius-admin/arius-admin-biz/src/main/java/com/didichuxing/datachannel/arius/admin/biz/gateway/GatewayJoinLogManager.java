package com.didichuxing.datachannel.arius.admin.biz.gateway;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayJoinVO;

public interface GatewayJoinLogManager {
    /**
     * 获取appId的错误gateway请求日志
     * @param appId 应用账号
     * @param startDate 开始时刻
     * @param endDate 结束时刻
     * @return list
     */
    Result<List<GatewayJoinVO>> getGatewayErrorList(Long appId, Long startDate, Long endDate);

    /**
     * 获取appId的慢查gateway请求日志
     * @param appId 应用账号
     * @param startDate 开始时刻
     * @param endDate 结束时刻
     * @return list
     */
    Result<List<GatewayJoinVO>> getGatewaySlowList(Long appId, Long startDate, Long endDate);

    /**
     * 根据appid获取指定数据中心一段时间查询量
     *
     * @param dataCenter
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    Result<Long> getSearchCountByProjectId(String dataCenter, Long appId, Long startDate, Long endDate);

    /**
     * 获取GatewaySlowList
     * @param appId 应用id
     * @param queryDTO 查询条件
     * @return Result<List<GatewayJoinVO>>
     */
    Result<List<GatewayJoinVO>> getGatewayJoinSlowList(Integer appId, GatewayJoinQueryDTO queryDTO);

    /**
     * 获取GatewayErrorList
     * @param appId 应用id
     * @param queryDTO 查询条件
     * @return Result<List<GatewayJoinVO>>
     */
    Result<List<GatewayJoinVO>> getGatewayJoinErrorList(Integer appId, GatewayJoinQueryDTO queryDTO);
}