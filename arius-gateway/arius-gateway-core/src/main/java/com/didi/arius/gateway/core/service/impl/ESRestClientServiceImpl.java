package com.didi.arius.gateway.core.service.impl;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author fitz
 * @date 2021/5/26 5:38 下午
 */
@Service
public class ESRestClientServiceImpl implements ESRestClientService {
    protected static final Logger bootLogger = LoggerFactory.getLogger(QueryConsts.BOOT_LOGGER);

    private static final int DEFAULT_MAX_CONN_PER_ROUTE = 500;
    private static final int DEFAULT_MAX_CONN_TOTAL = 3000;

    private static final String COLON = ":";
    private static final String COMMA = ",";

    private volatile Map<String, ESCluster> esClusterMap = new HashMap<>();

    @Autowired
    private QueryConfig queryConfig;

    @Override
    public ESClient getClient(String clusterName) {
        return getClientStrict(clusterName);
    }

    @Override
    public ESClient getClientStrict(String clusterName) {
        ESCluster esCluster = esClusterMap.get(clusterName);
        if (esCluster == null || esCluster.getEsClient() == null) {
            return null;
        }

        return esCluster.getEsClient();
    }

    @Override
    public ESClient getAdminClient() {
        return Objects.requireNonNull( esClusterMap.get(queryConfig.getAdminClusterName())).getEsClient();
    }

    @Override
    public void resetClients(Map<String, ESCluster> newDataCenterMap) {
        if (newDataCenterMap == null || newDataCenterMap.isEmpty()) {return;}

        List<String> noNeedClose = new ArrayList<>();
        for (Map.Entry<String, ESCluster> entry : newDataCenterMap.entrySet()) {
            String newClusterName = entry.getKey();
            ESCluster newESCluster = entry.getValue();
            try {
                if (this.esClusterMap.containsKey(newClusterName)) {
                    ESCluster oldESCluster = this.esClusterMap.get(newClusterName);
                    ESClient esClient = oldESCluster.getEsClient();
                    if (esClient != null && oldESCluster.getHttpAddress().equals( newESCluster.getHttpAddress())) {
                        esClient.setEsVersion( newESCluster.getEsVersion());
                        newESCluster.setEsClient(esClient);
                        noNeedClose.add(newClusterName);
                        continue;
                    }
                }
                bootLogger.info("add http dateCenter, cluster={}||addr={}", newESCluster.getCluster(), newESCluster.getHttpAddress());
                initClient( newESCluster );
            } catch (Exception e) {
                bootLogger.warn("add http dateCenter, cluster={}||addr={}", newESCluster.getCluster(), newESCluster.getHttpAddress(), e);
            }
        }

        final Map<String, ESCluster> oldDataCenterMap = this.esClusterMap;
        this.esClusterMap = Collections.unmodifiableMap(newDataCenterMap);

        for (Map.Entry<String, ESCluster> entry : oldDataCenterMap.entrySet()) {
            if (noNeedClose.contains(entry.getKey()) == false) {
                try {
                    entry.getValue().getEsClient().close();
                } catch (Exception e) {
                    bootLogger.warn("delete http dateCenter, cluster={}||addr={}", entry.getKey(), entry.getValue().getHttpAddress(), e);
                }
            }
            entry.getValue().setEsClient(null);
        }
    }

    @Override
    public Map<String, ESCluster> getESClusterMap() {
        return esClusterMap;
    }

    /************************************************************** private method **************************************************************/
    private void initClient(ESCluster esCluster) {
        if (esCluster.getEsClient() != null) {
            esCluster.getEsClient().close();
        }

        ESClient client = new ESClient( esCluster.getCluster(), esCluster.getEsVersion());
        client.setMax_conn_per_router(DEFAULT_MAX_CONN_PER_ROUTE);
        client.setMax_conn_total(DEFAULT_MAX_CONN_TOTAL);
        client.setSocket_timeout_millis(queryConfig.getEsSocketTimeout());

        String addr = esCluster.getHttpAddress();

        String[] hosts = StringUtils.split(addr, COMMA);
        for (int i = 0; i < hosts.length; ++i) {
            String clusterNode = hosts[i];

            String hostName = StringUtils.substringBeforeLast(clusterNode, COLON);
            String port = StringUtils.substringAfterLast(clusterNode, COLON);
            bootLogger.info("adding http client node={}||clusterName={}", clusterNode, esCluster.getCluster());
            try {
                client.addHttpHost(hostName, Integer.valueOf(port));
            } catch (Throwable e) {
                bootLogger.error("adding exception, http client node={}||clusterName={}", clusterNode, esCluster.getCluster(), e);
            }
        }

        if (!client.getEsVersion().startsWith(QueryConsts.ES_VERSION_2_PREFIX)) {
            client.addHeader(new BasicHeader("content-type", "application/json"));
        }

        client.start();

        esCluster.setEsClient(client);
    }
}
