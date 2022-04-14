package com.didi.arius.gateway.core.service;

import com.didi.arius.gateway.common.metadata.FlowThreshold;
import com.didi.arius.gateway.common.flowcontrol.FlowController;
import com.didi.arius.gateway.common.flowcontrol.FlowLimit;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public interface RateLimitService {

    /**
     * totalByteIn 字节添加计数
     * @param bytes
     */
    void addByteIn(long bytes);

    /**
     * totalByteIn 字节删除计数
     * @param bytes
     */
    void removeByteIn(long bytes);

    /**
     * 判断是否超过限流阈值
     * @param appid
     * @param searchId
     * @return
     */
    boolean isTrafficDataOverflow(int appid, String searchId);

    /**
     *
     * 流控计算
     * @param appid
     * @param searchId
     * @param in
     * @param out
     */
    void addUp(int appid, String searchId, int in, int out);

    /**
     * 重置流控值
     * @param appid
     * @param flowThreshold
     */
    void resetAppAreaFlow(int appid, FlowThreshold flowThreshold);

    /**
     * 获取 flowControllerMap
     * @return
     */
    Map<Integer, FlowController> getFlowControllerMap();

    /**
     * 获取 flowLimitMap
     * @return
     */
    ConcurrentMap<String, FlowLimit> getFlowLimitMap();

    /**
     * 获取 flowSchedulePeriod
     * @return
     */
    int getFlowSchedulePeriod();
}
