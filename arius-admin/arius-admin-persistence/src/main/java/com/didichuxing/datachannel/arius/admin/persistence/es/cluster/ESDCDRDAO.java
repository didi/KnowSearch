package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_TIMEOUT;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NullESClientException;
import com.didichuxing.datachannel.arius.admin.common.util.ParsingExceptionUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.knowframework.elasticsearch.client.ESClient;
import com.didiglobal.knowframework.elasticsearch.client.request.dcdr.*;
import com.didiglobal.knowframework.elasticsearch.client.response.dcdr.ESDeleteDCDRTemplateResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.dcdr.ESGetDCDRIndexResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.dcdr.ESGetDCDRTemplateResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.dcdr.ESPutDCDRTemplateResponse;
import com.google.common.collect.Lists;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Repository
public class ESDCDRDAO extends BaseESDAO {
    public static final String SECURITY_EXCEPTION="security_exception";
    /**
     * 保存dcdr模板配置 支持幂等
     * @param cluster 集群
     * @param name 名字
     * @param template 模板
     * @param replicaCluster 从集群
     * @return true/false
     */
    public boolean putAutoReplication(String cluster, String name, String template, String replicaCluster) throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            LOGGER.warn(
                    "class={}||method=putAutoReplication||clusterName={}||replicaCluster={}||template={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster, replicaCluster, template);
            throw new NullESClientException(cluster);
        }
        try {
            ESPutDCDRTemplateResponse response = client.admin().indices().preparePutDCDRTemplate().setName(name)
                    .setTemplate(template).setReplicaCluster(replicaCluster).execute()
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getAcknowledged();
        
        }catch (Exception e){
            final JSONObject exception = ParsingExceptionUtils.getResponseExceptionJsonMessageByException(e);
            final Optional<Boolean> securityExceptionOptional = Optional.ofNullable(exception)
                    .map(json -> json.getJSONObject(ERROR)).map(json -> json.getString(TYPE))
                    .map(type -> type.equalsIgnoreCase(SECURITY_EXCEPTION));
            if (securityExceptionOptional.isPresent() && Boolean.TRUE.equals(securityExceptionOptional.get())) {
                throw new ESOperateException(String.format("集群 %s 含账户名密码，创建 DCDR 链路失败", cluster));
            }
            LOGGER.error("class={}||method=putAutoReplication||clusterName={}||name={}", getClass().getSimpleName(),
                    cluster, name, e);
            ParsingExceptionUtils.abnormalTermination(e);

        }
        return false;
    }

    /**
     * 删除dcdr模板链路
     * @param cluster 集群
     * @param name 名字
     * @return true/false
     */
    public boolean deleteAutoReplication(String cluster, String name) throws ESOperateException {
        DCDRTemplate dcdrTemplate = getAutoReplication(cluster, name);
        if (dcdrTemplate == null) {
            return true;
        }
       

        ESClient client = esOpClient.getESClient(cluster);
        ESDeleteDCDRTemplateRequest request = new ESDeleteDCDRTemplateRequest();
        request.setName(name);
        ESDeleteDCDRTemplateResponse response =null;
    
        try {
        
            response = client.admin().indices().deleteDCDRTemplate(request)
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn("class={}||method=deleteReplication||clusterName={}||name={}", getClass().getSimpleName(),
                    cluster, name, e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return Optional.ofNullable(response).map(ESDeleteDCDRTemplateResponse::getAcknowledged).orElse(false);
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
            throw new NullESClientException(cluster);
        }
        
        ESGetDCDRTemplateRequest request = new ESGetDCDRTemplateRequest();
        request.setName(name);
        ESGetDCDRTemplateResponse response = null;
        try {
            response = client.admin().indices().getDCDRTemplate(request)
                    .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn("class={}||method=deleteReplication||clusterName={}||name={}", getClass().getSimpleName(),
                    cluster, name, e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return Optional.ofNullable(response).map(ESGetDCDRTemplateResponse::getDcdrs)
                .filter(CollectionUtils::isNotEmpty).map(dcdrTemplates -> dcdrTemplates.get(0)).orElse(null);

    }

    /**
     * 删除索引dcdr链路
     * @param cluster 集群
     * @param replicaCluster 从集群
     * @param indices 索引列表
     * @return result
     */
    public boolean deleteReplication(String cluster, String replicaCluster, Set<String> indices)
            throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            LOGGER.warn(
                    "class={}||method=deleteReplication||clusterName={}||replicaCluster={}||indices={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster, replicaCluster, indices);
            throw new NullESClientException(cluster);
        }
        try {
            ESGetDCDRIndexResponse getDCDRIndexResponse = client.admin().indices()
                    .getDCDRIndex(new ESGetDCDRIndexRequest()).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
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
        } catch (Exception e) {
            LOGGER.warn(
                    "class={}||method=deleteReplication||clusterName={}||replicaCluster={}||indices={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster, replicaCluster, indices);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return false;
    }
}