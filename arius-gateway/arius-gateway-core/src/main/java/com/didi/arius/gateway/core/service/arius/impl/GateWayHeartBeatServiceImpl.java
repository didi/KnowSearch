package com.didi.arius.gateway.core.service.arius.impl;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.arius.GateWayHeartBeatService;
import com.didi.arius.gateway.core.service.connectioncontrl.InboundConnectionHolder;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.ActiveCountResponse;
import javax.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class GateWayHeartBeatServiceImpl implements GateWayHeartBeatService {

    protected static final Logger bootLogger = LoggerFactory.getLogger( QueryConsts.BOOT_LOGGER);

    @Autowired
    private AriusAdminRemoteService ariusAdminRemoteService;
    @Autowired
    private InboundConnectionHolder inboundConnectionHolder;

    @Autowired
    private QueryConfig queryConfig;

    @Value("${gateway.nettyTransport.port}")
    private Integer port;

    @Value("${arius.gateway.adminSchedulePeriod}")
    private long adminSchedulePeriod;
    

    @Autowired
    private ThreadPool threadPool;

    private int currentNodeCount = 1;

    @PostConstruct
    public void init() {
        threadPool.submitScheduleAtFixTask(this::resetHeartBeatInfo, 15, adminSchedulePeriod);
    }

    @Override
    public void resetHeartBeatInfo(){
        try {
            heartbeat();
        } catch (Exception e) {
            bootLogger.error("heartbeat error", e);
        }
        try {
            resetActiveCount();
        } catch (Exception e) {
            bootLogger.error("resetActiveCount error", e);
        }

    }

    /************************************************************** private method **************************************************************/
    private void heartbeat(){
        ariusAdminRemoteService.heartbeat(queryConfig.getClusterName(), Convert.getIpAddr(), port,
                                          inboundConnectionHolder.connectionSize());
    }

    /**
     * 从admin获取存活的节点数量
     * 节点数量跟当前保存的节点数量不一致时，更新dsl限流值
     */
    private void resetActiveCount() {
        ActiveCountResponse response = ariusAdminRemoteService.getAliveCount(queryConfig.getClusterName());
        int activeCount = response.getData();

        if (activeCount < 1) {
            bootLogger.error("GateWayHeartBeatService alivecount error, activeCount={}", activeCount);
            return ;
        }

        if (activeCount == currentNodeCount) {
            bootLogger.info("resetActiveCount end, activeCount not change, activeCount={}", activeCount);
            return;
        }

        bootLogger.info("resetActiveCount activeCount changes, need resetDslRateLimit, activeCount={}, currentNodeCount={}", activeCount, currentNodeCount);

        currentNodeCount = activeCount;
    }
}