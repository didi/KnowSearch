package com.didichuxing.datachannel.arius.admin.biz.gateway;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.GatewayJoinVO;

import java.util.List;

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
}
