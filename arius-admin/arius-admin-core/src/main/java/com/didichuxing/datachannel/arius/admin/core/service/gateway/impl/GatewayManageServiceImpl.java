package com.didichuxing.datachannel.arius.admin.core.service.gateway.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.gateway.GatewayNode;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayNodePO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayManageService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayNodeDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author d06679
 * @date 2019-07-26
 */
@Service
public class GatewayManageServiceImpl implements GatewayManageService {

    private static final ILog LOGGER = LogFactory.getLog(GatewayManageServiceImpl.class);

    @Autowired
    private GatewayClusterDAO gatewayClusterDAO;

    @Autowired
    private GatewayNodeDAO    gatewayNodeDAO;

    private Set<String>       clusterNames;

    @PostConstruct
    public void init() {
        LOGGER.info("class=GatewayManageServiceImpl||method=init||GatewayManageServiceImpl init start.");
        reloadClusterName();
        LOGGER.info("class=GatewayManageServiceImpl||method=init||GatewayManageServiceImpl init finished.");
    }

    /**
     * gateway节点提交心跳
     *
     * @param heartbeat 心跳
     */
    @Override
    public Result heartbeat(GatewayHeartbeat heartbeat) {
        Result checkResult = checkHeartbeat(heartbeat);
        if (checkResult.failed()) {
            return checkResult;
        }

        if (!recordHeartbeat(heartbeat)) {
            return Result.buildFail("save db fail");
        }

        if (!clusterNames.contains(heartbeat.getClusterName())) {
            saveGatewayCluster(heartbeat.getClusterName());
        }

        return Result.buildSucc();
    }

    /**
     * 计算当前存活的节点数目
     *
     * @param clusterName 集群
     * @param gapTime     时间
     * @return count
     */
    @Override
    public Result<Integer> aliveCount(String clusterName, long gapTime) {
        if (AriusObjUtils.isNull(clusterName)) {
            return Result.buildFrom(Result.buildParamIllegal("cluster name is null"));
        }

        if (gapTime < 0) {
            return Result.buildFrom(Result.buildParamIllegal("gapTime name illegal"));
        }

        long time = System.currentTimeMillis() - gapTime;
        return Result.buildSucc(gatewayNodeDAO.aliveCountByClusterNameAndTime(clusterName, new Date(time)));
    }

    /**
     * 重新加载集群
     */
    @Override
    public void reloadClusterName() {
        clusterNames = Sets.newConcurrentHashSet();
        clusterNames.addAll(
            gatewayClusterDAO.listAll().stream().map(GatewayClusterPO::getClusterName).collect(Collectors.toSet()));
    }

    @Override
    public List<GatewayNode> getAliveNode(String clusterName, long timeout) {
        Date time = new Date(System.currentTimeMillis() - timeout);
        return ConvertUtil.list2List(gatewayNodeDAO.listAliveNodeByClusterNameAndTime(clusterName, time),
            GatewayNode.class);
    }

    private Result checkHeartbeat(GatewayHeartbeat heartbeat) {
        if (AriusObjUtils.isNull(heartbeat.getClusterName())) {
            return Result.buildParamIllegal("cluster name is null");
        }

        if (AriusObjUtils.isNull(heartbeat.getHostName())) {
            return Result.buildParamIllegal("host name is null");
        }

        if (AriusObjUtils.isNull(heartbeat.getPort())) {
            return Result.buildParamIllegal("port is null");
        }

        if (heartbeat.getPort() < 1) {
            return Result.buildParamIllegal("port illegal");
        }
        return Result.buildSucc();
    }

    private boolean recordHeartbeat(GatewayHeartbeat heartbeat) {
        GatewayNodePO gatewayNodePO = new GatewayNodePO();
        gatewayNodePO.setClusterName(heartbeat.getClusterName().trim());
        gatewayNodePO.setHeartbeatTime(new Date());
        gatewayNodePO.setHostName(heartbeat.getHostName().trim());
        gatewayNodePO.setPort(heartbeat.getPort());
        return gatewayNodeDAO.record(gatewayNodePO)==1;
    }

    private void saveGatewayCluster(String clusterName) {
        GatewayClusterPO gatewayClusterPO = new GatewayClusterPO();
        gatewayClusterPO.setClusterName(clusterName);
        if (1 == gatewayClusterDAO.insert(gatewayClusterPO)) {
            clusterNames.add(clusterName);
        }
    }
}
