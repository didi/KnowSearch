package com.didi.arius.gateway.core.service.arius.impl;

import com.didi.arius.gateway.common.consts.QueryConsts;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.didi.arius.gateway.common.utils.CommonUtil.isSearchKibana;

@Service
public class ESClusterServiceImpl implements ESClusterService {
    protected static final Logger bootLogger = LoggerFactory.getLogger( QueryConsts.BOOT_LOGGER);

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
        threadPool.submitScheduleAtFixTask( () -> resetESClusaterInfo(), 0, schedulePeriod);
    }

    @Override
    public Map<String, ESCluster> listESCluster(){
        return esClusterMap;
    }

    @Override
    public MetaVersion getMetaVersionByCluster(String cluster){
        MetaVersion metaVersion = versionMap.get(cluster);
        if(null == metaVersion){
            metaVersion = new MetaVersion();
            versionMap.put(cluster, metaVersion);
        }

        return metaVersion;
    }

    @Override
    public void resetESClusaterInfo(){
        resetESClusterMap();
    }

    @Override
    public ESClient getClient(QueryContext queryContext) {
        ESClient client = esRestClientService.getClient(queryContext.getCluster());

        if (isSearchKibana(queryContext.getUri(), queryContext.getIndices())) {
            client = esRestClientService.getAdminClient();
        } else if (queryContext.getClusterId() != null){
            client = esRestClientService.getClientStrict(queryContext.getClusterId());
            if (client == null) {
                throw new ClusterNotFoundException("cluster not found:" + queryContext.getClusterId());
            }
        }

        queryContext.setClient(client);
        if (client != null) {
            queryContext.setClusterName(client.getClusterName());
        }

        return client;
    }

    @Override
    public ESClient getClient(QueryContext queryContext, IndexTemplate indexTemplate) {
        ESClient client = esRestClientService.getClient(queryContext.getCluster());
        String clusterName = queryContext.getCluster();

        if (isSearchKibana(queryContext.getUri(), queryContext.getIndices())) {
            client = esRestClientService.getAdminClient();
            clusterName = queryConfig.getAdminClusterName();
        } else if (queryContext.getClusterId() != null){
            client = esRestClientService.getClientStrict(queryContext.getClusterId());
            clusterName = queryContext.getClusterId();
        } else if (indexTemplate != null) {
            boolean findSlave = false;
            if (indexTemplate.getSlaveInfos() != null && indexTemplate.getSlaveInfos().size() > 0) {
                for (TemplateClusterInfo info : indexTemplate.getSlaveInfos()) {
                    if (info.getAccessApps().contains(queryContext.getAppid())) {
                        client = esRestClientService.getClient(info.getCluster());
                        clusterName = info.getCluster();
                        findSlave = true;
                        break;
                    }
                }
            }

            if (false == findSlave) {
                client = esRestClientService.getClient(indexTemplate.getMasterInfo().getCluster());
                clusterName = indexTemplate.getMasterInfo().getCluster();
            }
        }

        if (client == null) {
            throw new ClusterNotFoundException("cluster not found:" + clusterName);
        }

        queryContext.setClient(client);
        queryContext.setClusterName(client.getClusterName());

        return client;
    }

    @Override
    public ESClient getClientFromCluster(QueryContext queryContext, String clusterName) {
        ESClient readClient = esRestClientService.getClient(clusterName);

        if (readClient == null) {
            throw new ClusterNotFoundException("cluster not found:" + clusterName);
        }

        queryContext.setClient(readClient);
        queryContext.setClusterName(readClient.getClusterName());

        return readClient;
    }

    @Override
    public ESClient getWriteClient(IndexTemplate indexTemplate) {
        String masterCluster = indexTemplate.getMasterInfo().getCluster();
        ESClient writeClient = esRestClientService.getClient(masterCluster);

        if (writeClient == null) {
            throw new ClusterNotFoundException("cluster not found:" + masterCluster);
        }

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

            if (queryConfig.getRunMode().equals( QueryConsts.GATEWAY_WRITE_MODE)) {
                esCluster.setHttpAddress(dataCenterResponse.getHttpWriteAddress());
            } else {
                esCluster.setHttpAddress(dataCenterResponse.getHttpAddress());
            }

            esCluster.setType( ESCluster.Type.IntegerToType(dataCenterResponse.getType()));

            if (dataCenterResponse.getEsVersion() == null || dataCenterResponse.getEsVersion().equals("")) {
                esCluster.setEsVersion(QueryConsts.DEFAULT_ES_VERSION);
            } else {
                esCluster.setEsVersion(dataCenterResponse.getEsVersion());
            }
            esCluster.setPassword(dataCenterResponse.getPassword());

            newESClusterMap.put(esCluster.getCluster(), esCluster );

            if (false == versionMap.containsKey(dataCenterResponse.getCluster())) {
                MetaVersion version = new MetaVersion();
                versionMap.put(esCluster.getCluster(), version);
            }
        }

        bootLogger.info("resetESClusterMap done,old esClusterMap size={}, new esClusterMap size={}", esClusterMap.size(), newESClusterMap.size());

        esClusterMap = newESClusterMap;
        initESClient(newESClusterMap);
    }

    private void initESClient(Map<String, ESCluster> newESClusterMap){
        esTcpClientService.resetClients(newESClusterMap);
        esRestClientService.resetClients(newESClusterMap);
    }
}
