package com.didi.arius.gateway.core.service.arius.impl;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.enums.RunModeEnum;
import com.didi.arius.gateway.common.exception.ClusterNotFoundException;
import com.didi.arius.gateway.common.metadata.*;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.core.service.ESTcpClientService;
import com.didi.arius.gateway.core.service.arius.ESClusterService;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.DataCenterListResponse;
import com.didi.arius.gateway.remote.response.DataCenterResponse;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.didi.arius.gateway.common.consts.RestConsts.DEFAULT_WRITE_ACTION;
import static com.didi.arius.gateway.common.utils.CommonUtil.isSearchKibana;

@Service
@NoArgsConstructor
public class ESClusterServiceImpl implements ESClusterService {
    protected static final Logger logger = LoggerFactory.getLogger(ESClusterServiceImpl.class);
    protected static final Logger bootLogger = LoggerFactory.getLogger( QueryConsts.BOOT_LOGGER);
    public static final String CLUSTER_NOT_FOUND = "cluster not found:";
    public static final String COLON = ":";
    public static final String COMMA = ",";

    @Autowired
    private AriusAdminRemoteService ariusAdminRemoteService;

    @Autowired
    private ThreadPool threadPool;

    @Autowired
    private ESTcpClientService esTcpClientService;

    @Autowired
    private ESRestClientService esRestClientService;

    @Autowired
    private QueryConfig queryConfig;

    @Value("${arius.gateway.adminSchedulePeriod}")
    private long schedulePeriod;

    private Map<String, ESCluster> esClusterMap = new HashMap<>();
    private Map<String, MetaVersion> versionMap = new HashMap<>();

    @PostConstruct
    public void init(){
        threadPool.submitScheduleAtFixTask(this::resetESClusaterInfo, 0, schedulePeriod);
    }

    @Override
    public Map<String, ESCluster> listESCluster(){
        return esClusterMap;
    }

    @Override
    public MetaVersion getMetaVersionByCluster(String cluster){
        return versionMap.computeIfAbsent( cluster, s -> new MetaVersion() );
    }

    @Override
    public void resetESClusaterInfo(){
        try {
            resetESClusterMap();
        } catch (Exception e) {
            bootLogger.error("resetESClusterMap error", e);
        }
    }

    @Override
    public ESClient getClient(QueryContext queryContext, String actionName) {
        ESClient client = esRestClientService.getClient(queryContext.getCluster(), actionName);

        if (isSearchKibana(queryContext.getUri(), queryContext.getIndices())) {
            client = esRestClientService.getAdminClient(actionName);
        } else if (queryContext.getClusterId() != null){
            client = esRestClientService.getClientStrict(queryContext.getClusterId(), actionName);
            validClient(client, queryContext.getClusterId());
        }

        queryContext.setClient(client);
        if (client != null) {
            queryContext.setClusterName(client.getClusterName());
        }

        return client;
    }

    @Override
    public ESClient getClient(QueryContext queryContext, IndexTemplate indexTemplate, String actionName) {
        ESClient client = esRestClientService.getClient(queryContext.getCluster(), actionName);
        String clusterName = queryContext.getCluster();

        if (isSearchKibana(queryContext.getUri(), queryContext.getIndices())) {
            client = esRestClientService.getAdminClient(actionName);
            clusterName = queryConfig.getAdminClusterName();
        } else if (queryContext.getClusterId() != null){
            client = esRestClientService.getClientStrict(queryContext.getClusterId(), actionName);
            clusterName = queryContext.getClusterId();
        } else if (indexTemplate != null) {
            boolean findSlave = false;
            if (indexTemplate.getSlaveInfos() != null && !indexTemplate.getSlaveInfos().isEmpty()) {
                for (TemplateClusterInfo info : indexTemplate.getSlaveInfos()) {
                    if (info.getAccessApps().contains(queryContext.getAppid())) {
                        client = esRestClientService.getClient(info.getCluster(), actionName);
                        clusterName = info.getCluster();
                        findSlave = true;
                        break;
                    }
                }
            }

            if (!findSlave) {
                client = esRestClientService.getClient(indexTemplate.getMasterInfo().getCluster(), actionName);
                clusterName = indexTemplate.getMasterInfo().getCluster();
            }
        }

        validClient(client, clusterName);

        queryContext.setClient(client);
        queryContext.setClusterName(client.getClusterName());

        return client;
    }

    private void validClient(ESClient client, String clusterName) {
        if (client == null) {
            throw new ClusterNotFoundException(CLUSTER_NOT_FOUND + clusterName);
        }
    }

    @Override
    public ESClient getClientFromCluster(QueryContext queryContext, String clusterName, String actionName) {
        ESClient readClient = esRestClientService.getClient(clusterName, actionName);

        validClient(readClient, clusterName);

        queryContext.setClient(readClient);
        queryContext.setClusterName(readClient.getClusterName());

        return readClient;
    }

    @Override
    public ESClient getWriteClient(IndexTemplate indexTemplate, String actionName) {
        String masterCluster = indexTemplate.getMasterInfo().getCluster();
        ESClient writeClient = esRestClientService.getClient(masterCluster, actionName);

        validClient(writeClient, masterCluster);

        return writeClient;
    }

    /************************************************************** private method **************************************************************/
    /**
     * 更新集群列表
     */
    private void resetESClusterMap() {
        DataCenterListResponse response = ariusAdminRemoteService.listCluster();

        Map<String, ESCluster> newESClusterMap = new HashMap<>();
        for (DataCenterResponse dataCenterResponse : response.getData()) {
            if (dataCenterResponse.getHttpAddress() == null || dataCenterResponse.getHttpAddress().isEmpty()) {
                bootLogger.warn("ESCluster httpAddress is empty, ESCluster={}", dataCenterResponse.getCluster());
                continue;
            }

            ESCluster esCluster = new ESCluster();
            esCluster.setCluster(dataCenterResponse.getCluster());
            esCluster.setReadAddress(dataCenterResponse.getReadAddress());
            esCluster.setHttpAddress(dataCenterResponse.getHttpAddress());
            esCluster.setHttpWriteAddress(dataCenterResponse.getHttpWriteAddress());
            esCluster.setRunMode(dataCenterResponse.getRunMode());
            initWriteAction(dataCenterResponse, esCluster);
            bootLogger.info("dataCenter httpAddress[{}] httpWriteAddress[{}] runMode[{}] writeAction[{}]", esCluster.getHttpAddress(),
                    esCluster.getHttpWriteAddress(), esCluster.getRunMode(), esCluster.getWriteAction());

            esCluster.setType( ESCluster.Type.integerToType(dataCenterResponse.getType()));

            if (dataCenterResponse.getEsVersion() == null || dataCenterResponse.getEsVersion().equals("")) {
                esCluster.setEsVersion(QueryConsts.DEFAULT_ES_VERSION);
            } else {
                esCluster.setEsVersion(dataCenterResponse.getEsVersion());
            }
            esCluster.setPassword(dataCenterResponse.getPassword());

            newESClusterMap.put(esCluster.getCluster(), esCluster );

            if (!versionMap.containsKey(dataCenterResponse.getCluster())) {
                MetaVersion version = new MetaVersion();
                versionMap.put(esCluster.getCluster(), version);
            }
        }

        bootLogger.info("resetESClusterMap done,old esClusterMap size={}, new esClusterMap size={}", esClusterMap.size(), newESClusterMap.size());

        esClusterMap = newESClusterMap;

        String esClusterLog = JSON.toJSONString(esClusterMap);
        bootLogger.info("esClusterMap now {}", esClusterLog);

        initESClient(newESClusterMap);
    }

    private void initESClient(Map<String, ESCluster> newESClusterMap){
        esTcpClientService.resetClients(newESClusterMap);
        esRestClientService.resetClients(newESClusterMap);
    }

    /**
     * 初始化读写分离的writeAction
     * @param dataCenterResponse admin返回的response
     * @param dataCenter 构建的数据中心
     */
    private void initWriteAction(DataCenterResponse dataCenterResponse, ESCluster dataCenter) {
        if (dataCenterResponse.getRunMode() == RunModeEnum.READ_WRITE_SPLIT.getRunMode()) {
            dataCenter.setWriteAction(Strings.isNullOrEmpty(dataCenterResponse.getWriteAction()) ?
                    DEFAULT_WRITE_ACTION :
                    Sets.newHashSet(dataCenterResponse.getWriteAction().split(COMMA)));
        } else {
            dataCenter.setWriteAction(new HashSet<>());
        }
    }
}
