package com.didichuxing.datachannel.arius.admin.core.service.gateway.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.gateway.GatewayNode;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayNodePO;
import com.didichuxing.datachannel.arius.admin.common.constant.GatewaySqlConstant;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayNodeDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class GatewayServiceImpl implements GatewayService {

    private static final ILog LOGGER = LogFactory.getLog(GatewayServiceImpl.class);

    @Autowired
    private GatewayClusterDAO gatewayClusterDAO;

    @Autowired
    private ESGatewayClient esGatewayClient;

    @Autowired
    private GatewayNodeDAO    gatewayNodeDAO;

    @Autowired
    private AppService appService;

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
    public Result<Void> heartbeat(GatewayHeartbeat heartbeat) {
        Result<Void> checkResult = checkHeartbeat(heartbeat);
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
            return Result.buildParamIllegal("cluster name is null");
        }

        if (gapTime < 0) {
            return Result.buildParamIllegal("gapTime name illegal");
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

    @Override
    public Result<String> sqlOperate(String sql, Integer appId, String postFix) {
        Result<String> result = preSqlParamCheck(sql, appId, postFix);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        String url = GatewaySqlConstant.DEFAULT_HTTP_PRE_FIX + esGatewayClient.getGatewayAddress() + postFix;
        try {
            // gateway的sql语句操作接口直接以字符串的形式返还结果
            String sqlResponse = BaseHttpUtil.postForString(url, sql, buildGatewayHeader(appId.toString()));
            return Result.buildSucc(sqlResponse, "");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("class=GatewayManageServiceImpl||method=directSqlSearch||postFix={}||errMsg={}", postFix, e);
        }
        return Result.buildFail();
    }

    private Result<String> preSqlParamCheck(String sql, Integer appId, String postFix) {
        if (StringUtils.isBlank(sql)) {
            return Result.buildParamIllegal("查询的sql语句为空");
        }
        if (StringUtils.isBlank(postFix)) {
            return Result.buildParamIllegal("查询gateway的路径后缀为空");
        }
        List<Integer> appIds = appService.listApps().stream().map(App::getId).collect(Collectors.toList());
        if (appId == null || !appIds.contains(appId)) {
            return Result.buildParamIllegal("对应的appId字段非法");
        }
        return Result.buildSucc();
    }

    private Map<String, String> buildGatewayHeader(String appId) throws UnsupportedEncodingException {
        Map<String, String> headers = Maps.newHashMap();
        BasicHeader basicHeader = (BasicHeader) esGatewayClient.getAppidHeaderMap().get(appId);
        headers.put("Content-Type", "application/json;charset=utf-8");
        headers.put(basicHeader.getName(), basicHeader.getValue());
        headers.put("X-ARIUS-APP-ID", appId);
        return headers;
    }

    private Result<Void> checkHeartbeat(GatewayHeartbeat heartbeat) {
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
        return gatewayNodeDAO.recordGatewayNode(gatewayNodePO)==1;
    }

    private void saveGatewayCluster(String clusterName) {
        GatewayClusterPO gatewayClusterPO = new GatewayClusterPO();
        gatewayClusterPO.setClusterName(clusterName);
        if (1 == gatewayClusterDAO.insert(gatewayClusterPO)) {
            clusterNames.add(clusterName);
        }
    }
}
