package com.didichuxing.datachannel.arius.admin.core.service.gateway.impl;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.gateway.GatewayClusterNode;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import com.didichuxing.datachannel.arius.admin.common.constant.GatewaySqlConstant;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterNodeDAO;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.NoArgsConstructor;

/**
 * @author didi
 */
@Service
@NoArgsConstructor
public class GatewayServiceImpl implements GatewayService {

    private static final ILog     LOGGER = LogFactory.getLog(GatewayServiceImpl.class);

    @Autowired
    private GatewayClusterDAO     gatewayClusterDAO;

    @Autowired
    private ESGatewayClient       esGatewayClient;
    
    @Autowired
    private GatewayClusterNodeDAO  gatewayClusterNodeDAO;
    @Autowired
    private ComponentDomainService componentDomainService;

    private Set<String>           clusterNames;

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
        return Result.buildSucc(gatewayClusterNodeDAO.aliveCountByClusterNameAndTime(clusterName, new Date(time)));
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
    public List<GatewayClusterNode> getAliveNode(String clusterName, long timeout) {
        Date time = new Date(System.currentTimeMillis() - timeout);
        return ConvertUtil.list2List(gatewayClusterNodeDAO.listAliveNodeByClusterNameAndTime(clusterName, time),
            GatewayClusterNode.class);
    }

    @Override
    public Result<String> sqlOperate(String sql, String phyClusterName, ESUser esUser, String postFix) {
        Result<String> result = preSqlParamCheck(sql, postFix);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        String url = GatewaySqlConstant.DEFAULT_HTTP_PRE_FIX + esGatewayClient.getSingleGatewayAddress() + postFix;

        try {
            // gateway的sql语句操作接口直接以字符串的形式返还结果
            String sqlResponse = BaseHttpUtil.postForString(url, sql, buildGatewayHeader(phyClusterName, esUser));
            return Result.buildSucc(sqlResponse, "");
        } catch (Exception e) {
            LOGGER.error("class=GatewayManageServiceImpl||method=directSqlSearch||postFix={}||errMsg={}", postFix, e);
        }
        return Result.buildFail();
    }

    private Result<String> preSqlParamCheck(String sql, String postFix) {
        if (StringUtils.isBlank(sql)) {
            return Result.buildParamIllegal("查询的sql语句为空");
        }
        if (StringUtils.isBlank(postFix)) {
            return Result.buildParamIllegal("查询gateway的路径后缀为空");
        }

        return Result.buildSucc();
    }

    private Map<String, String> buildGatewayHeader(String phyClusterName, ESUser esUser) {
        Map<String, String> headers = Maps.newHashMap();

        Header header = BaseHttpUtil.buildHttpHeader(String.valueOf(esUser.getId()), esUser.getVerifyCode());
        headers.put("Content-Type", "application/json;charset=utf-8");
        headers.put(header.getName(), header.getValue());
        if (!AriusObjUtils.isBlack(phyClusterName)) {
            headers.put("CLUSTER-ID", phyClusterName);
        }
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
        GatewayClusterNodePO gatewayClusterNodePO = ConvertUtil.obj2Obj(heartbeat,GatewayClusterNodePO.class);
        gatewayClusterNodePO.setClusterName(heartbeat.getClusterName().trim());
        gatewayClusterNodePO.setHeartbeatTime(new Date());
        final GatewayClusterPO clusterPO = gatewayClusterDAO.getOneByName(
            heartbeat.getClusterName().trim());
        if (Objects.nonNull(clusterPO) && Objects.nonNull(clusterPO.getComponentId())
            && clusterPO.getComponentId() > 0) {
            final com.didiglobal.logi.op.manager.infrastructure.common.Result<Component> component = componentDomainService.getComponentById(
                    clusterPO.getComponentId());
            if (component.isSuccess() && Objects.nonNull(component.getData())) {
                final List<ComponentHost> hostList = component.getData().getHostList();
                if (CollectionUtils.isNotEmpty(hostList)) {
                    hostList.stream().filter(
                                    i -> StringUtils.equals(StringUtils.trim(heartbeat.getHostName()), i.getHost())).findFirst()
                            .map(ComponentHost::getMachineSpec).ifPresent(gatewayClusterNodePO::setMachineSpec);
                }
            }
        }
        final List<GatewayClusterNodePO> gatewayClusterNodePOS = gatewayClusterNodeDAO.selectByClusterName(
                heartbeat.getClusterName());
        if (CollectionUtils.isNotEmpty(gatewayClusterNodePOS)){
            final Optional<GatewayClusterNodePO> clusterNodePOOptional = gatewayClusterNodePOS.stream().filter(
                    i -> StringUtils.equals(gatewayClusterNodePO.getClusterName(), i.getClusterName())
                         && StringUtils.equals(gatewayClusterNodePO.getHostName(), i.getHostName()) && Objects.equals(
                            gatewayClusterNodePO.getPort(), i.getPort())).findFirst();
            if (clusterNodePOOptional.isPresent()) {
                gatewayClusterNodePO.setId(clusterNodePOOptional.get().getId());
                return gatewayClusterNodeDAO.update(gatewayClusterNodePO);
            }
    
        }
        return gatewayClusterNodeDAO.recordGatewayNode(gatewayClusterNodePO) > 0;
    }

    private void saveGatewayCluster(String clusterName) {
        GatewayClusterPO gatewayClusterPO = new GatewayClusterPO();
        gatewayClusterPO.setClusterName(clusterName);
        if (1 == gatewayClusterDAO.insert(gatewayClusterPO)) {
            clusterNames.add(clusterName);
        }
    }
}