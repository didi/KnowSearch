package com.didichuxing.datachannel.arius.admin.persistence.component;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.ClusterDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.gateway.document.ESIndexRequest;
import com.didichuxing.datachannel.arius.elasticsearch.client.gateway.document.ESIndexResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.batch.BatchType;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.batch.ESBatchRequest;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.index.refreshindex.ESIndicesRefreshIndexRequest;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.batch.ESBatchResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.batch.IndexResultItemNode;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.deletebyquery.ESIndicesDeleteByQueryResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.refreshindex.ESIndicesRefreshIndexResponse;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/14 下午1:22
 * @Modified By
 *
 * 更新es数据的客户端，数据只会写入arius-meta集群
 */
@Component
public class ESUpdateClient {

    private final ILog                    LOGGER            = LogFactory.getLog(ESUpdateClient.class);

    /**
     * 集群名称
     */
    @Value("${es.update.cluster.name}")
    private String                        clusterName;

    /**
     * 批量执行操作的记录数
     */
    private final int                     executeBatchSize  = 50;
    /**
     * 客户端个数
     */
    private int                           clientCount       = 15;
    /**
     *  更新es数据的客户端连接队列
     */
    private LinkedBlockingQueue<ESClient> updateClientPool  = new LinkedBlockingQueue<>(clientCount);
    /**
     * 之前的http连接地址
     */
    private String                        beforeHttpAddress = null;

    @Autowired
    private ClusterDAO                    clusterDAO;

    public final static int               MAX_RETRY_CNT     = 5;

    private static final String           COMMA             = ",";

    @PostConstruct
    public void init() {
        LOGGER.info("class=ESUpdateClient||method=init||ESUpdateClient init start.");
        List<ClusterPO> clusterPOS = clusterDAO.listAll();
        setDataSourceList(clusterPOS);
        LOGGER.info("class=ESUpdateClient||method=init||ESUpdateClient init finished.");
    }

    // delete by query
    public ESIndicesDeleteByQueryResponse deleteByQuery(String index, String type, String query) {
        ESClient esClient = getUpdateEsClientFromPool();
        if (esClient == null) {
            LOGGER.warn("class=ESUpdateClient||method=deleteByQuery||errMsg=esClient is null");
            return null;
        }

        ESIndicesDeleteByQueryResponse resp = esClient.admin().indices().prepareDeleteByQuery().setIndex(index)
            .setType(type).setQuery(query).execute().actionGet(5, TimeUnit.MINUTES);
        if (resp == null) {
            LOGGER.warn("class=UpdateClient||method=deleteByQuery||errMsg=resp is null");
            return null;
        }

        return resp;
    }

    /**
     * 写入单条
     *
     * @param source
     * @return
     */
    public boolean index(String indexName, String typeName, String id, String source) {
        ESClient esClient = null;
        ESIndexResponse response = null;

        try {
            esClient = getUpdateEsClientFromPool();
            if (esClient == null) {
                return false;
            }

            ESIndexRequest esIndexRequest = new ESIndexRequest();
            esIndexRequest.setIndex(indexName);
            esIndexRequest.type(typeName);
            esIndexRequest.source(source);
            esIndexRequest.id(id);

            for (int i = 0; i < MAX_RETRY_CNT; ++i) {
                response = esClient.index(esIndexRequest).actionGet(10, TimeUnit.SECONDS);
                if (response == null) {
                    continue;
                }

                return response.getRestStatus().getStatus() == HttpStatus.SC_OK
                       || response.getRestStatus().getStatus() == HttpStatus.SC_CREATED;
            }

        } catch (Exception e) {
            LOGGER.warn(
                "class=UpdateClient||method=index||indexName={}||typeName={}||id={}||source={}||errMsg=index doc error. ",
                indexName, typeName, id, source, e);
            if (response != null) {
                LOGGER.warn(
                    "class=UpdateClient||method=index||indexName={}||typeName={}||id={}||source={}||errMsg=response {}",
                    indexName, typeName, id, source, JSONObject.toJSONString(response));
            }
        } finally {
            if (esClient != null) {
                returnUpdateEsClientToPool(esClient);
            }
        }

        return false;
    }

    /**
     * 批量写入
     *
     * @param indexName
     * @param typeName
     * @return
     */
    public boolean batchInsert(String indexName, String typeName, List<? extends BaseESPO> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return true;
        }

        ESClient esClient = null;
        ESBatchResponse response = null;
        try {
            esClient = getUpdateEsClientFromPool();
            if (esClient == null) {
                return false;
            }

            ESBatchRequest batchRequest = new ESBatchRequest();
            for (BaseESPO po : pos) {
                batchRequest.addNode(BatchType.INDEX, indexName, typeName, po.getKey(), JSONObject.toJSONString(po));
            }

            for (int i = 0; i < MAX_RETRY_CNT; ++i) {
                response = esClient.batch(batchRequest).actionGet(2, TimeUnit.MINUTES);
                if (response == null) {
                    continue;
                }

                // 日志信息详细
                int errorItemIndex = 0;

                if (response.getErrors()) {
                    if (CollectionUtils.isNotEmpty(response.getItems())) {
                        for (IndexResultItemNode item : response.getItems()) {
                            if (item.getIndex() != null && item.getIndex().getShards() != null
                                && CollectionUtils.isNotEmpty(item.getIndex().getShards().getFailures())) {
                                LOGGER.warn(
                                    "class=UpdateClient||method=batchInsert||indexName={}||typeName={}||errMsg=Failures: {}, content: {}",
                                    indexName, typeName, item.getIndex().getShards().getFailures().toString(),
                                    JSONObject.toJSONString(pos.get(errorItemIndex)));
                            }

                            if (item.getIndex() != null && item.getIndex().getError() != null) {
                                LOGGER.warn(
                                    "class=UpdateClient||method=batchInsert||indexName={}||typeName={}||errMsg=Error: {}, content: {}",
                                    indexName, typeName, item.getIndex().getError().getReason(),
                                    JSONObject.toJSONString(pos.get(errorItemIndex)));
                            }

                            ++errorItemIndex;
                        }
                    }

                    return false;
                }

                return response.getRestStatus().getStatus() == HttpStatus.SC_OK && !response.getErrors();

            }
        } catch (Exception e) {
            LOGGER.warn(
                "class=UpdateClient||method=batchInsert||indexName={}||typeName={}||errMsg=batch insert error. ",
                indexName, typeName, e);
            if (response != null) {
                LOGGER.warn("class=UpdateClient||method=batchInsert||indexName={}||typeName={}||errMsg=response {}",
                    indexName, typeName, JSONObject.toJSONString(response));
            }

        } finally {
            if (esClient != null) {
                returnUpdateEsClientToPool(esClient);
            }
        }

        return false;
    }

    /**
     * 批量删除
     *
     * @param indexName
     * @param typeName
     * @param ids
     * @return
     */
    public boolean batchDelete(String indexName, String typeName, List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }

        ESClient esClient = null;
        ESBatchResponse response = null;
        try {
            esClient = getUpdateEsClientFromPool();
            if (esClient == null) {
                return false;
            }

            ESBatchRequest batchRequest = new ESBatchRequest();
            for (String id : ids) {
                batchRequest.addNode(BatchType.DELETE, indexName, typeName, id, "");
            }

            for (int i = 0; i < MAX_RETRY_CNT; ++i) {
                response = esClient.batch(batchRequest).actionGet(2, TimeUnit.MINUTES);
                if (response == null) {
                    continue;
                }

                return response.getRestStatus().getStatus() == HttpStatus.SC_OK && !response.getErrors();
            }
        } catch (Exception e) {
            LOGGER.warn(
                "class=UpdateClient||method=batchDelete||indexName={}||typeName={}||errMsg=batch delete error. ",
                indexName, typeName, e);
            if (response != null) {
                LOGGER.warn("class=UpdateClient||method=batchDelete||indexName={}||typeName={}||errMsg=response {}",
                    indexName, typeName, JSONObject.toJSONString(response));
            }

        } finally {
            if (esClient != null) {
                returnUpdateEsClientToPool(esClient);
            }
        }

        return false;
    }

    /**
     * 批量更新
     *
     * @param indexName
     * @param typeName
     * @param pos
     * @return
     */
    public boolean batchUpdate(String indexName, String typeName, List<? extends BaseESPO> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return true;
        }

        ESClient esClient = null;
        ESBatchResponse response = null;
        try {
            esClient = getUpdateEsClientFromPool();
            if (esClient == null) {
                return false;
            }

            ESBatchRequest batchRequest = new ESBatchRequest();
            for (BaseESPO po : pos) {
                batchRequest.addNode(BatchType.UPDATE, indexName, typeName, po.getKey(), JSONObject.toJSONString(po));
            }

            for (int i = 0; i < MAX_RETRY_CNT; ++i) {
                response = esClient.batch(batchRequest).actionGet(2, TimeUnit.MINUTES);
                if (response == null) {
                    continue;
                }

                return response.getRestStatus().getStatus() == HttpStatus.SC_OK && !response.getErrors();
            }
        } catch (Exception e) {
            LOGGER.warn(
                "class=UpdateClient||method=batchUpdate||indexName={}||typeName={}||errMsg=batch update error. ",
                indexName, typeName, e);
            if (response != null) {
                LOGGER.warn("class=UpdateClient||method=batchUpdate||indexName={}||typeName={}||errMsg=response {}",
                    indexName, typeName, JSONObject.toJSONString(response));
            }

        } finally {
            if (esClient != null) {
                returnUpdateEsClientToPool(esClient);
            }
        }

        return false;
    }

    /**
     * 刷新索引
     *
     * @param indexName
     * @return
     */
    public boolean refreshIndex(String indexName) {
        if (StringUtils.isBlank(indexName)) {
            return false;
        }

        ESClient esClient = null;
        try {
            esClient = getUpdateEsClientFromPool();
            if (esClient == null) {
                return false;
            }

            ESIndicesRefreshIndexRequest request = new ESIndicesRefreshIndexRequest();
            request.setIndex(indexName);

            ESIndicesRefreshIndexResponse response = null;
            response = esClient.admin().indices().refreshIndex(request).actionGet(10, TimeUnit.SECONDS);

            return response.getRestStatus().getStatus() == HttpStatus.SC_OK;
        } catch (Exception e) {
            LOGGER.warn("class=UpdateClient||method=refreshIndex||indexName={}||errMsg=refresh index error. ",
                indexName, e);

        } finally {
            if (esClient != null) {
                returnUpdateEsClientToPool(esClient);
            }
        }

        return false;
    }

    /**************************************** private method ****************************************************/
    /**
     * 从更新es http 客户端连接池找那个获取
     *
     * @return
     */
    private ESClient getUpdateEsClientFromPool() {
        ESClient esClient = null;
        int retryCount = 0;

        // 如果esClient为空或者重试次数小于5次，循环获取
        while (esClient == null && retryCount < 5) {
            try {
                ++retryCount;
                esClient = updateClientPool.poll(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {

            }
        }

        if (esClient == null) {
            LOGGER.error(
                "class=ESUpdateClient||method=getUpdateEsClientFromPool||errMsg=fail to get es client from pool");
        }

        return esClient;
    }

    /**
     * 归还到es http 客户端连接池
     *
     * @param esClient
     */
    private void returnUpdateEsClientToPool(ESClient esClient) {
        try {
            this.updateClientPool.put(esClient);
        } catch (InterruptedException e) {

        }
    }

    private ESClient buildEsClient(String address, String clusterName) {
        if (StringUtils.isBlank(address)) {
            return null;
        }

        try {
            String[] httpAddressArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(address, COMMA);
            TransportAddress[] transportAddresses = new TransportAddress[httpAddressArray.length];
            ESClient esClient = new ESClient();

            for (int i = 0; i < httpAddressArray.length; ++i) {
                String[] httpAddressAndPortArray = StringUtils
                    .splitByWholeSeparatorPreserveAllTokens(httpAddressArray[i], ":");
                if (httpAddressAndPortArray != null && httpAddressAndPortArray.length == 2) {
                    transportAddresses[i] = new InetSocketTransportAddress(
                        new InetSocketAddress(httpAddressAndPortArray[0], Integer.valueOf(httpAddressAndPortArray[1])));
                }
            }
            esClient.addTransportAddresses(transportAddresses);
            if (StringUtils.isNotBlank(clusterName)) {
                esClient.setClusterName(clusterName);
            }
            // 配置http超时
            esClient.setRequestConfigCallback(builder -> builder.setConnectTimeout(10000).setSocketTimeout(120000)
                .setConnectionRequestTimeout(120000));
            esClient.start();

            return esClient;
        } catch (Exception e) {
            LOGGER.error("class=ESUpdateClient||method=buildEsClient||errMsg={}||address={}", e.getMessage(), address,
                e);
            return null;
        }
    }

    /**
     * 初始化访问es集群的客户端
     *
     */
    private void setDataSourceList(List<ClusterPO> dataSourceList) {
        if (dataSourceList == null) {
            LOGGER.error("class=ESUpdateClient||method=setDataSourceList||errMsg=fail to get es clusters");
            return;
        }

        ClusterPO updateClusterDataSource = null;
        for (ClusterPO dataSource : dataSourceList) {
            if (clusterName.equals(dataSource.getCluster())) {
                updateClusterDataSource = dataSource;
                break;
            }
        }

        if (updateClusterDataSource == null) {
            LOGGER.error("class=UpdateClient||method=setDataSourceList||errMsg=fail to get es cluster info {}",
                clusterName);
            return;
        }

        // 判断地址是否发生变化
        if (beforeHttpAddress != null && updateClusterDataSource.getHttpAddress().equals(beforeHttpAddress)) {
            return;
        }

        // 移除以存在的客户端
        try {
            ESClient beforeEsClient = null;
            Iterator<ESClient> iterator = this.updateClientPool.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    beforeEsClient = iterator.next();
                    if (beforeEsClient != null) {
                        beforeEsClient.close();
                        beforeEsClient = null;
                    }
                    iterator.remove();
                    LOGGER.info("class=UpdateClient||method=setDataSourceList||msg=remove old es client {}, {}",
                        clusterName, beforeHttpAddress);
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=UpdateClient||method=setDataSourceList||errMsg=fail to remove old es client {}",
                clusterName, e);
        }

        this.updateClientPool.clear();
        beforeHttpAddress = updateClusterDataSource.getHttpAddress();

        // 添加新的客户端
        ESClient esClient = null;
        for (int i = 0; i < clientCount; ++i) {
            esClient = buildEsClient(beforeHttpAddress, clusterName);
            if (esClient != null) {
                this.updateClientPool.add(esClient);
                LOGGER.info("class=UpdateClient||method=setDataSourceList||msg=add new es client {}, {}", clusterName,
                    beforeHttpAddress);
            }
        }
    }
}
