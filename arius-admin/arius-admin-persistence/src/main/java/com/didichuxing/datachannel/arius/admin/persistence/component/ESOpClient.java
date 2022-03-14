package com.didichuxing.datachannel.arius.admin.persistence.component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.ClusterDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * es的操作需要通过ESOpClient，维护admin与各个集群的链接
 * @author d06679
 * @date 2019/3/20
 */
@Component
public class ESOpClient {

    private static final ILog            LOGGER       = LogFactory.getLog(ESOpClient.class);

    private final Map<String, ESClient>  esClientMap  = new ConcurrentHashMap<>();
    private final Map<String, ClusterPO> esClusterMap = new ConcurrentHashMap<>();

    @Autowired
    private ClusterDAO                   clusterDAO;

    @Value("${es.client.io.thread.count:0}")
    private Integer                      ioThreadCount;

    /**
     * 启动就链接所有集群
     * 每10min就检查一遍自己的链接
     *
     * Client信息最近10分钟有更新的会更新对应的Client信息，
     * Client的信息会8个小时重新连接一遍，做为兜底方案
     */
    @PostConstruct
    @Scheduled(cron = "0 3/5 * * * ?")
    public synchronized void refreshConnect() {

        LOGGER.info("class=ESOpClient||method=init||ESOpClient refreshConnect start.");
        List<ClusterPO> dataCluster = clusterDAO.listAll();
        Set<String> currentESClientClusters = Sets.newHashSet(esClientMap.keySet());
        dataCluster.parallelStream().forEach(clusterPO -> {

            if (!esClientMap.containsKey(clusterPO.getCluster())) {
                try {
                    connect(clusterPO);
                } catch (Exception e) {
                    LOGGER.error("class=ESOpClient||method=refreshConnect||errMsg={}||cluster={}", e.getMessage(),
                            clusterPO.getCluster(), e);
                }
            } else {
                ClusterPO cachedCluster = esClusterMap.get(clusterPO.getCluster());
                if (cachedCluster != null && !cachedCluster.equals(clusterPO)) {
                    LOGGER.info("class=ESOpClient||method=refreshConnect||msg=clusterMetaUpdate||" +
                                    "cluster={}||cachedClusterMeta={}||currentClusterMeta={}",
                            clusterPO.getCluster(), JSON.toJSONString(cachedCluster), JSON.toJSONString(clusterPO));
                    reConnect(clusterPO.getCluster());
                } else if (cachedCluster == null) {
                    LOGGER.error("class=ESOpClient||method=refreshConnect||msg=clusterCachedMiss||" +
                                    "cluster={}||currentClusterMeta={}",
                            clusterPO.getCluster(), JSON.toJSONString(clusterPO));
                }
            }
            currentESClientClusters.remove(clusterPO.getCluster());
        });

        if (CollectionUtils.isNotEmpty(currentESClientClusters)) {
            for (String cluster : currentESClientClusters) {
                removeAndCloseESClient(cluster);
            }
        }
        LOGGER.info("class=ESOpClient||method=init||ESOpClient refreshConnect finished.");
    }

    /**
     * 链接
     * @param cluster 集群名字
     */
    public synchronized void connect(String cluster) {
        if (esClientMap.containsKey(cluster)) {
            return;
        }

        connect(clusterDAO.getByName(cluster));
    }

    /**
     * 移除某个集群的链接
     * @param cluster 集群名称
     */
    public void removeAndCloseESClient(String cluster) {
        LOGGER.info("class=ESOpClient||method=removeAndCloseESClient||msg=remove es client||cluster={}", cluster);

        ESClient client = esClientMap.remove(cluster);
        if (client != null) {
            client.close();
        }

        esClusterMap.remove(cluster);
    }

    /**
     * 获取某个集群的链接
     * @param cluster 集群名称
     * @return 链接
     */
    public ESClient getESClient(String cluster) {
        if (!esClientMap.containsKey(cluster)) {
            LOGGER.warn("class=ESOpClient||method=getESClient||msg=cluster connect not exist, reconnect||cluster={}", cluster);
        }

        return esClientMap.get(cluster);
    }

    /**
     * 重新链接
     * @param cluster 集群名字
     */
    public void reConnect(String cluster) {
        removeAndCloseESClient(cluster);
        connect(cluster);
    }

    /**************************************** private method ****************************************************/
    private void connect(ClusterPO clusterPO) {
        LOGGER.info("class=ESOpClient||method=connect||msg=connect es start||cluster={}", clusterPO.getCluster());

        if (StringUtils.isBlank(clusterPO.getHttpAddress())) {
            LOGGER.warn("class=ESOpClient||method=connect||msg=connect es fail, httpAddress is null||cluster={}", clusterPO.getCluster());
            return;
        }

        ESClient client = new ESClient();
        client.addTransportAddresses(clusterPO.getHttpAddress());
        if (StringUtils.isNotEmpty(clusterPO.getPassword())) {
            client.setPassword(clusterPO.getPassword());
        }

        try {
            if (ESClient.DEFAULT_ES_VERSION.equals(client.getEsVersion())
                    && !ESClient.DEFAULT_ES_VERSION.equals(clusterPO.getEsVersion())) {
                client.setEsVersion(clusterPO.getEsVersion());
                client.setClusterName(clusterPO.getCluster());
            }

            if (ioThreadCount > 0) {
                client.setIoThreadCount(ioThreadCount);
            }

            client.start();

            ESClusterHealthResponse response = client.admin().cluster().prepareHealth().execute().actionGet(10,
                    TimeUnit.SECONDS);

            if (RestStatus.OK.getStatus() == response.getRestStatus().getStatus()) {
                LOGGER.info("class=ESOpClient||method=connect||msg=connect es by http succ||cluster={}||hosts={}", clusterPO.getCluster(),
                        clusterPO.getHttpAddress());

                esClientMap.put(clusterPO.getCluster(), client);
                esClusterMap.put(clusterPO.getCluster(), clusterPO);
            }else {
                client.close();
            }
        }catch (Exception e){
            client.close();
            LOGGER.error("class=ESOpClient||method=connect||msg=connect es by http error||cluster={}||hosts={}||msg=client start error", clusterPO.getCluster(),
                    clusterPO.getHttpAddress());
        }
    }
}
