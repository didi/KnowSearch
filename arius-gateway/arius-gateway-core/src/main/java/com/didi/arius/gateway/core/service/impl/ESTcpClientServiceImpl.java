package com.didi.arius.gateway.core.service.impl;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.service.ESTcpClientService;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.*;

import static org.apache.commons.lang.StringUtils.*;

/**
 * @author fitz
 * @date 2021/5/26 5:34 下午
 */
@Service
public class ESTcpClientServiceImpl implements ESTcpClientService {
    protected static final Logger bootLogger = LoggerFactory.getLogger(QueryConsts.BOOT_LOGGER);

    private static final String COLON = ":";
    private static final String COMMA = ",";

    @Value("${elasticsearch.admin.cluster.name}")
    private String adminClusterName;

    @Autowired
    private QueryConfig queryConfig;

    private Map<String, ESCluster> dataCenterMap = new HashMap<>();

    @Override
    public Client getClient(String clusterName) {
        return Objects.requireNonNull(dataCenterMap.get(clusterName)).getClient();
    }

    @Override
    public Client getAdminClient() {
        return Objects.requireNonNull(dataCenterMap.get(adminClusterName)).getClient();
    }

    @Override
    public void resetClients(Map<String, ESCluster> newDataCenterMap) {
        List<ESCluster> adds = new ArrayList<>();
        List<ESCluster> deletes = new ArrayList<>();

        // check update and delete
        for (Map.Entry<String, ESCluster> entry : dataCenterMap.entrySet()) {
            String clusterName = entry.getKey();
            ESCluster ESCluster = entry.getValue();

            if (newDataCenterMap.containsKey(clusterName) == false) {
                // delete
                deletes.add( ESCluster );
                continue;
            }

            ESCluster newESCluster = newDataCenterMap.get(clusterName);
            if (ESCluster.getReadAddress().equals( newESCluster.getReadAddress()) == false) {
                adds.add( newESCluster );
            }
        }

        // check add
        for (Map.Entry<String, ESCluster> entry : newDataCenterMap.entrySet()) {
            String clusterName = entry.getKey();
            ESCluster newESCluster = entry.getValue();

            if (false == newESCluster.getEsVersion().startsWith(QueryConsts.ES_VERSION_2_PREFIX)) {
                bootLogger.info("tcp client reject high version, cluster={}, version={}", clusterName, newESCluster.getEsVersion());
                continue;
            }

            if (dataCenterMap.containsKey(clusterName) == false) {
                adds.add( newESCluster );
            }
        }

        for (ESCluster ESCluster : deletes) {
            bootLogger.info("delete dateCenter, cluster={}||addr={}", ESCluster.getCluster(), ESCluster.getReadAddress());
            ESCluster.getClient().close();
            dataCenterMap.remove( ESCluster );
        }

        for (ESCluster ESCluster : adds) {
            bootLogger.info("add dateCenter, cluster={}||addr={}", ESCluster.getCluster(), ESCluster.getReadAddress());

            //reset client
            initClient( ESCluster );

            dataCenterMap.put( ESCluster.getCluster(), ESCluster );
        }
    }

    @Override
    public Map<String, ESCluster> getDataCenterMap() {
        return dataCenterMap;
    }

    @Override
    public void setDataCenterMap(Map<String, ESCluster> dataCenterMap) {
        this.dataCenterMap = dataCenterMap;
    }

    /************************************************************** private method **************************************************************/
    private void initClient(ESCluster ESCluster) {
        if (ESCluster.getClient() != null) {
            ESCluster.getClient().close();
        }

        String addr = ESCluster.getReadAddress();
        Settings settings = Settings.builder()
                .put("cluster.name", ESCluster.getCluster())
                .put("transport.tcp.connect_timeout", queryConfig.getConnectESTime())
                .put("transport.netty.worker_count", queryConfig.getClientWorkerCount())
                .build();
        TransportClient client = TransportClient.builder().settings(settings).build();

        ESCluster.setClient(client);
        for (String clusterNode : split(addr, COMMA)) {
            String hostName = substringBeforeLast(clusterNode, COLON);
            String port = substringAfterLast(clusterNode, COLON);
            bootLogger.info("adding transport node={}||clusterName={}", clusterNode, ESCluster.getCluster());
            try {
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostName), Integer.valueOf(port)));
            } catch (Throwable e) {
                bootLogger.error("adding exception, transport node={}||clusterName={}", clusterNode, ESCluster.getCluster(), e);
            }
        }
        client.connectedNodes();
    }
}
