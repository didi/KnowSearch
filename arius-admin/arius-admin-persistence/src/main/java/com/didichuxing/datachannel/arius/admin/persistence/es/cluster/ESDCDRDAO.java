package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_TIMEOUT;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.request.dcdr.DCDRIndex;
import com.didiglobal.logi.elasticsearch.client.request.dcdr.DCDRTemplate;
import com.didiglobal.logi.elasticsearch.client.request.dcdr.ESDeleteDCDRTemplateRequest;
import com.didiglobal.logi.elasticsearch.client.request.dcdr.ESGetDCDRIndexRequest;
import com.didiglobal.logi.elasticsearch.client.request.dcdr.ESGetDCDRTemplateRequest;
import com.didiglobal.logi.elasticsearch.client.response.dcdr.ESDeleteDCDRTemplateResponse;
import com.didiglobal.logi.elasticsearch.client.response.dcdr.ESGetDCDRIndexResponse;
import com.didiglobal.logi.elasticsearch.client.response.dcdr.ESGetDCDRTemplateResponse;
import com.didiglobal.logi.elasticsearch.client.response.dcdr.ESPutDCDRTemplateResponse;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Repository
public class ESDCDRDAO extends BaseESDAO {

    /**
     * 保存dcdr模板配置 支持幂等
     * @param cluster 集群
     * @param name 名字
     * @param template 模板
     * @param replicaCluster 从集群
     * @return true/false
     */
    public boolean putAutoReplication(String cluster, String name, String template, String replicaCluster) {
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            LOGGER.warn("class={}||method=putAutoReplication||clusterName={}||replicaCluster={}||template={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster, replicaCluster,template);
            return false;
        }
        ESPutDCDRTemplateResponse response = client.admin().indices().preparePutDCDRTemplate().setName(name)
            .setTemplate(template).setReplicaCluster(replicaCluster).execute()
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        return response.getAcknowledged();
    }

    /**
     * 删除dcdr模板链路
     * @param cluster 集群
     * @param name 名字
     * @return true/false
     */
    public boolean deleteAutoReplication(String cluster, String name) {
        try {
            DCDRTemplate dcdrTemplate = getAutoReplication(cluster, name);
            if (dcdrTemplate == null) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("class={}||method=deleteAutoReplication||clusterName={}||name={}", getClass().getSimpleName(),
                    cluster, name, e);
            return false;
        }
       

        ESClient client = esOpClient.getESClient(cluster);
        ESDeleteDCDRTemplateRequest request = new ESDeleteDCDRTemplateRequest();
        request.setName(name);
        ESDeleteDCDRTemplateResponse response = client.admin().indices().deleteDCDRTemplate(request)
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        return response.getAcknowledged();
    }

    /**
     * 获取dcdr模板
     * @param cluster 集群
     * @param name 名字
     * @return dcdr模板
     */
    public DCDRTemplate getAutoReplication(String cluster, String name) throws ESOperateException {
        //如果集群挂掉，就是可以抛出NPE
        ESClient client = esOpClient.getESClient(cluster);
        if (client==null){
            throw new ESOperateException(String.format("cluster:【%s】无法获取到es client",cluster));
        }
        
        ESGetDCDRTemplateRequest request = new ESGetDCDRTemplateRequest();
        request.setName(name);

        ESGetDCDRTemplateResponse response = client.admin().indices().getDCDRTemplate(request)
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        List<DCDRTemplate> dcdrTemplates = response.getDcdrs();
        if (CollectionUtils.isEmpty(dcdrTemplates)) {
            return null;
        }

        return dcdrTemplates.get(0);

    }

    /**
     * 删除索引dcdr链路
     * @param cluster 集群
     * @param replicaCluster 从集群
     * @param indices 索引列表
     * @return result
     */
    public boolean deleteReplication(String cluster, String replicaCluster, Set<String> indices) {
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            LOGGER.warn("class={}||method=deleteReplication||clusterName={}||replicaCluster={}||indices={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster, replicaCluster,indices);
            return false;
        }
        
        ESGetDCDRIndexResponse getDCDRIndexResponse = client.admin().indices().getDCDRIndex(new ESGetDCDRIndexRequest())
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        List<DCDRIndex> allIndexDCDRs = getDCDRIndexResponse.getDcdrs();

        List<String> shouldDels = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(allIndexDCDRs)) {
            for (DCDRIndex index : allIndexDCDRs) {
                if (index.getReplicaCluster().equals(replicaCluster) && indices.contains(index.getPrimaryIndex())) {
                    shouldDels.add(index.getPrimaryIndex());
                }
            }
        }

        if (CollectionUtils.isEmpty(shouldDels)) {
            return true;
        }

        boolean succ = true;
        for (String delIndex : shouldDels) {
            succ = succ && client.admin().indices().prepareDeleteDCDRIndex().setPrimaryIndex(delIndex)
                .setReplicaIndex(delIndex).setReplicaCluster(replicaCluster).execute()
                .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS).getAcknowledged();
        }

        return succ;
    }
}