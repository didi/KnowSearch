package com.didi.arius.gateway.core.service.impl;

import java.util.*;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.enums.RunModeEnum;
import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang.StringUtils.*;

/**
 * @author fitz
 * @date 2021/5/26 5:38 下午
 */
@Service
public class ESRestClientServiceImpl implements ESRestClientService {
    protected static final Logger logger = LoggerFactory.getLogger( ESRestClientServiceImpl.class);
    protected static final Logger bootLogger = LoggerFactory.getLogger(QueryConsts.BOOT_LOGGER);

    private static final int DEFAULT_MAX_CONN_PER_ROUTE = 500;
    private static final int DEFAULT_MAX_CONN_TOTAL = 3000;

    private static final String COLON = ":";
    private static final String COMMA = ",";

    private volatile Map<String, ESCluster> esClusterMap = new HashMap<>();

    @Autowired
    private QueryConfig queryConfig;

    @Override
    public ESClient getClient(String clusterName, String actionName) {
        return getClientStrict(clusterName, actionName);
    }

    @Override
    public ESClient getClientStrict(String clusterName, String actionName) {
        ESCluster dataCenter = esClusterMap.get(clusterName);
        if (dataCenter == null) {
            return null;
        }
        ESClient esClient = dataCenter.getEsClient();
        if (dataCenter.getWriteAction().contains(actionName)) {
            esClient = dataCenter.getEsWriteClient();
            bootLogger.debug("assign action[{}], request action[{}] is write, write client host:[{}]",
                    dataCenter.getWriteAction(), actionName, esClient.getNodes());
        }
        return esClient;

    }

    @Override
    public ESClient getAdminClient(String actionName) {
        Objects.requireNonNull(esClusterMap.get(queryConfig.getAdminClusterName()));
        return getClient(queryConfig.getAdminClusterName(), actionName);

    }

    @Override
    public void resetClients(Map<String, ESCluster> newDataCenterMap) {
        if (newDataCenterMap == null || newDataCenterMap.isEmpty()) return;
        List<String> noNeedClose = new ArrayList<>();
        for (Map.Entry<String, ESCluster> entry : newDataCenterMap.entrySet()) {
            String newClusterName = entry.getKey();
            ESCluster newDataCenter = entry.getValue();
            try {
                if (this.esClusterMap.containsKey(newClusterName)) {
                    if (alreadyInitialCenter(noNeedClose, newClusterName, newDataCenter)) {
                        clientKeepalive(newDataCenter);
                        continue;
                    }
                }
                bootLogger.info("add http dateCenter, cluster={}||addr={}", newDataCenter.getCluster(), newDataCenter.getHttpAddress());
                initClient(newDataCenter);
            } catch (Exception e) {
                bootLogger.warn("add http dateCenter, cluster={}||addr={}", newDataCenter.getCluster(), newDataCenter.getHttpAddress(), e);
            }
        }
        final Map<String, ESCluster> oldDataCenterMap = this.esClusterMap;
        this.esClusterMap = Collections.unmodifiableMap(newDataCenterMap);

        for (Map.Entry<String, ESCluster> entry : oldDataCenterMap.entrySet()) {
            if (noNeedClose.contains(entry.getKey()) == false) {
                closeOldClient(entry);
            }
            /*entry.getValue().setEsClient(null);*/
        }
    }

    /**
     *  es client的报活
     * @param newDataCenter 集群
     */
    private void clientKeepalive(ESCluster newDataCenter) {
        ESClient esClient = newDataCenter.getEsClient();
        ESClient esWriteClient = newDataCenter.getEsWriteClient();
        if (null != esClient && !esClient.isActualRunning()) {
            logger.warn(String.format("cluster[%s] client is stop, start rebuild", esClient.getClusterName()));
            esClient.start();
        }
        if (null != esWriteClient && !esWriteClient.isActualRunning()) {
            logger.warn(String.format("cluster[%s] write client is stop, start rebuild", esWriteClient.getClusterName()));
            esWriteClient.start();
        }
    }

    @Override
    public Map<String, ESCluster> getESClusterMap() {
        return esClusterMap;
    }

    private void closeOldClient(Map.Entry<String, ESCluster> entry) {
        try {
            entry.getValue().getEsClient().close();
            if (null != entry.getValue().getEsWriteClient()) {
                entry.getValue().getEsWriteClient().close();
            }
        } catch (Exception e) {
            bootLogger.warn("delete http dateCenter, cluster={}||addr={}", entry.getKey(), entry.getValue().getHttpAddress(), e);
        }
    }

    private boolean alreadyInitialCenter(List<String> noNeedClose, String newClusterName, ESCluster newDataCenter) {
        ESCluster oldDataCenter = this.esClusterMap.get(newClusterName);
        ESClient esClient = oldDataCenter.getEsClient();
        ESClient esWriteClient = oldDataCenter.getEsWriteClient();
        if (isEqualAddress(oldDataCenter.getHttpAddress(), newDataCenter.getHttpAddress()) &&
                isEqualAddress(oldDataCenter.getHttpWriteAddress(), newDataCenter.getHttpWriteAddress()) &&
                oldDataCenter.getRunMode() == newDataCenter.getRunMode()) {
            esClient.setEsVersion(newDataCenter.getEsVersion());
            if (null != esWriteClient) {
                esWriteClient.setEsVersion(newDataCenter.getEsVersion());
            }
            newDataCenter.setEsWriteClient(esWriteClient);
            newDataCenter.setEsClient(esClient);
            noNeedClose.add(newClusterName);
            return true;
        }
        return false;
    }

    private void initClient(ESCluster dataCenter) {
        initReadClient(dataCenter);
        if (dataCenter.getRunMode() == RunModeEnum.READ_WRITE_SPLIT.getRunMode()) {
            initWriteClient(dataCenter);
        }
    }

    private void initReadClient(ESCluster dataCenter) {
        ESClient esOldClient = dataCenter.getEsClient();
        dataCenter.setEsClient(getEsClient(dataCenter, dataCenter.getHttpAddress()));
        if (null != esOldClient) {
            esOldClient.close();
        }
    }

    private void initWriteClient(ESCluster dataCenter) {
        ESClient esOldWriteClient = dataCenter.getEsWriteClient();
        dataCenter.setEsWriteClient(getEsClient(dataCenter, dataCenter.getHttpWriteAddress()));
        if (null != esOldWriteClient) {
            esOldWriteClient.close();
        }
    }

    private ESClient getEsClient(ESCluster dataCenter, String addr) {
        ESClient client = new ESClient(dataCenter.getCluster(), dataCenter.getEsVersion());
        client.setMax_conn_per_router(DEFAULT_MAX_CONN_PER_ROUTE);
        client.setMax_conn_total(DEFAULT_MAX_CONN_TOTAL);

        client.setSocket_timeout_millis(queryConfig.getEsSocketTimeout());
        String[] hosts = split(addr, COMMA);
        for (int i = 0; i < hosts.length; ++i) {
            String clusterNode = hosts[i];

            String hostName = substringBeforeLast(clusterNode, COLON);
            String port = substringAfterLast(clusterNode, COLON);
            bootLogger.info("adding http client node={}||clusterName={}", clusterNode, dataCenter.getCluster());
            try {
                client.addHttpHost(hostName, Integer.valueOf(port));
            } catch (Exception e) {
                bootLogger.error("adding exception, http client node={}||clusterName={}", clusterNode, dataCenter.getCluster(), e);
            }
        }

        //开源gateway为了支持带认证的集群使用gateway访问
		if (!Strings.isEmpty(dataCenter.getPassword())) {
			client.setBasicAuth(dataCenter.getPassword());
		}

        if (!client.getEsVersion().startsWith(QueryConsts.ES_VERSION_2_PREFIX)) {
            client.addHeader(new BasicHeader("content-type", "application/json"));
        }

        client.start();
        return client;
    }

    public boolean isEqualAddress(String oneAddress, String otherAddress) {
        if (null == oneAddress && null == otherAddress) {
            return true;
        }
        if (null == oneAddress || null == otherAddress) {
            return false;
        }
        if (oneAddress.equals(otherAddress)) {
            return true;
        }
        return false;
    }
}
