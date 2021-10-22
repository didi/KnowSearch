package com.didi.arius.gateway.core.service.arius.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.TooManyIndexException;
import com.didi.arius.gateway.common.metadata.*;
import com.didi.arius.gateway.common.utils.IndexTire;
import com.didi.arius.gateway.common.utils.IndexTireBuilder;
import com.didi.arius.gateway.common.utils.Regex;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.core.service.arius.ESClusterService;
import com.didi.arius.gateway.core.service.arius.IndexTemplateService;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectResponse;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.*;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.gateway.GatewayException;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class IndexTemplateServiceImpl implements IndexTemplateService {

    private static final Logger bootLogger = LoggerFactory.getLogger( QueryConsts.BOOT_LOGGER);
    private static final Logger logger = LoggerFactory.getLogger(IndexTemplateServiceImpl.class);

    @Autowired
    private AriusAdminRemoteService ariusAdminRemoteService;

    @Autowired
    private ESClusterService esClusterService;

    @Autowired
    private ThreadPool threadPool;

    @Value("${arius.gateway.adminSchedulePeriod}")
    private long schedulePeriod;

    @Autowired
    private ESRestClientService esRestClientService;

    private IndexTire indexTire;

    private Map<String, Map<String, TemplateInfo>> templateExpressionMap = new HashMap<>();
    private Map<String, Map<String, TemplateInfo>> templateAliasesMap 	 = new HashMap<>();

    private Map<String, IndexTemplate>  indexTemplateMap = new HashMap<>();
    private Map<String, String>         indexToAlias     = new HashMap<>();


    @PostConstruct
    public void init(){
        threadPool.submitScheduleAtFixTask( () -> resetIndexTemplateInfo(), 0, schedulePeriod );
    }

    @Override
    public Map<String, Map<String, TemplateInfo>> getTemplateExpressionMap() {
        return templateExpressionMap;
    }

    @Override
    public Map<String, Map<String, TemplateInfo>> getTemplateAliasMap() {
        return templateAliasesMap;
    }

    @Override
    public IndexTemplate getIndexTemplate(String template) {
        return indexTemplateMap.get(template);
    }

    @Override
    public IndexTemplate getIndexTemplateByTire(String index) throws TooManyIndexException {
        if (indexTire == null) {
            return null;
        }

        return indexTire.search(index);
    }

    @Override
    public Map<String, IndexTemplate> getIndexTemplateMap() {
        return indexTemplateMap;
    }

    @Override
    public void resetIndexTemplateInfo(){
        resetTemplateDetail();
        resetTemplateInfo();
        resetIndexAlias();
    }

    @Override
    public String getIndexAlias(String index) {
        return indexToAlias.get(index);
    }

    @Override
    public boolean checkIndex(String index, List<String> indexExps) {
        if (index.startsWith(".")) {
            return true;
        }

        boolean matched = false;
        for (String indexExp : indexExps) {
            if (Regex.expContainIndex(index, indexExp)) {
                logger.debug("index permitted, index={}, indexExp={}", index, indexExp);
                matched = true;
                break ;
            }
        }

        return matched;
    }

    @Override
    public int getIndexVersion(String indexName, String cluster) {
        if (indexName == null) {
            return 0;
        }

        TemplateInfo templateInfo = getAliasesMatch(indexName, cluster);
        if (templateInfo != null) {
            return templateInfo.getVersion();
        }

        templateInfo = getExpressionMatch(indexName, cluster);
        if (templateInfo != null) {
            return templateInfo.getVersion();
        }

        return 0;
    }

    @Override
    public IndexTemplate getTemplateByIndexTire(List<String> indices, QueryContext queryContext) throws IndexNotFoundException, TooManyIndexException{
        String index = indices.get(0);

        IndexTemplate indexTemplate = getIndexTemplateByTire(index);
        if (indexTemplate == null) {
            String alias = getIndexAlias(index);
            if (alias != null) {
                indexTemplate = getIndexTemplateByTire(alias);
            }
        }

        if (indexTemplate == null) {
            throw new IndexNotFoundException("query can't find index template exception,index=" + index);
        }

        for (int i = 1; i < indices.size(); ++i) {
            boolean check = IndexTire.checkIndexMatchTemplate(indices.get(i), indexTemplate);
            if (!check) {
                String alias = getIndexAlias(indices.get(i));
                if (alias != null) {
                    check = IndexTire.checkIndexMatchTemplate(alias, indexTemplate);
                }
            }

            if (!check) {
                throw new TooManyIndexException(String.format("search query indices don't have the same index template, index1=%s, index2=%s", index, indices.get(i)));
            }
        }

        queryContext.setIndexTemplate(indexTemplate);

        return indexTemplate;
    }

    /************************************************************** private method **************************************************************/
    /**
     * 更新各集群模板、大基数mapping等信息
     */
    private void resetTemplateDetail() {
        for (Map.Entry<String, ESCluster> entry : esClusterService.listESCluster().entrySet()) {
            String cluster = entry.getKey();

            TemplateInfoListResponse templateInfoListResponse = ariusAdminRemoteService.getTemplateInfoMap(cluster);

            resetTemplateMap(cluster, templateInfoListResponse);
        }
    }

    /**
     * 更新索引模板
     * @param cluster 集群名称
     * @param response 索引模板接口数据
     */
    private void resetTemplateMap(String cluster, TemplateInfoListResponse response) {
        Map<String, TemplateInfo> expressionMap = new HashMap<>();
        Map<String, TemplateInfo> aliasesMap = new HashMap<>();

        Map<String, TemplateInfo> oldExpressionMap = templateExpressionMap.get(cluster);

        for (Map.Entry<String, TemplateInfoResponse> entry : response.getData().entrySet()) {
            TemplateInfoResponse templateInfoResponse = entry.getValue();

            boolean needSource = false;

            TemplateInfo templateInfo = new TemplateInfo();
            templateInfo.setNeedSource(needSource);
            templateInfo.setVersion(templateInfoResponse.getVersion());

            if (oldExpressionMap != null) {
                if (oldExpressionMap.containsKey(templateInfoResponse.getExpression())) {
                    TemplateInfo oldTemplateInfo = oldExpressionMap.get(templateInfoResponse.getExpression());
                    templateInfo.setMappings(oldTemplateInfo.getMappings());
                }
            }

            expressionMap.put(templateInfoResponse.getExpression(), templateInfo);

            for (AliasesInfoResponse aliasesInfoResponse : templateInfoResponse.getAliases()) {
                aliasesMap.put(aliasesInfoResponse.getName(), templateInfo);
            }
        }

        templateExpressionMap.put(cluster, expressionMap);
        templateAliasesMap.put(cluster, aliasesMap);

        bootLogger.info("cluster={}||expressionMap size={}||aliasesMap size={}", cluster, expressionMap.size(), aliasesMap.size());
    }

    /**
     * 更新逻辑索引模板列表
     */
    private void resetTemplateInfo() {
        bootLogger.info("resetTemplateInfo begin...");

        Map<String, IndexTemplate> newIndexTemplateMap = new HashMap<>();

        IndexTemplateListResponse response = ariusAdminRemoteService.listDeployInfo();

        for (Map.Entry<String, IndexTemplateResponse> entry : response.getData().entrySet()) {
            IndexTemplateResponse indexTemplateResponse = entry.getValue();
            IndexTemplate indexTemplate = new IndexTemplate();

            indexTemplate.setName(entry.getKey());
            indexTemplate.setId(indexTemplateResponse.getBaseInfo().getId());
            indexTemplate.setDateField(indexTemplateResponse.getBaseInfo().getDateField());
            indexTemplate.setDateFormat(indexTemplateResponse.getBaseInfo().getDateFormat());
            indexTemplate.setExpireTime(indexTemplateResponse.getBaseInfo().getExpireTime());
            indexTemplate.setExpression(indexTemplateResponse.getBaseInfo().getExpression());
            indexTemplate.setIsDefaultRouting(indexTemplateResponse.getBaseInfo().getIsDefaultRouting());
            indexTemplate.setVersion(indexTemplateResponse.getBaseInfo().getVersion());
            indexTemplate.setDeployStatus(IndexTemplate.DeployStatus.IntegerToStatus(indexTemplateResponse.getBaseInfo().getDeployStatus()));
            indexTemplate.setAliases(indexTemplateResponse.getBaseInfo().getAliases());
            indexTemplate.setIngestPipeline(indexTemplateResponse.getBaseInfo().getIngestPipeline());

            TemplateClusterInfo masterInfo = new TemplateClusterInfo();

            masterInfo.setAccessApps(indexTemplateResponse.getMasterInfo().getAccessApps() == null ? new HashSet<>() : new HashSet<>(indexTemplateResponse.getMasterInfo().getAccessApps()));
            masterInfo.setCluster(indexTemplateResponse.getMasterInfo().getCluster());
            masterInfo.setTopic(indexTemplateResponse.getMasterInfo().getTopic());
            masterInfo.setMappingIndexNameEnable( Objects.isNull(indexTemplateResponse.getMasterInfo().getMappingIndexNameEnable()) ? false : indexTemplateResponse.getMasterInfo().getMappingIndexNameEnable());
            masterInfo.setTypeIndexMapping( MapUtils.isEmpty(indexTemplateResponse.getMasterInfo().getTypeIndexMapping()) ? Maps.newHashMap() : indexTemplateResponse.getMasterInfo().getTypeIndexMapping());

            indexTemplate.setMasterInfo(masterInfo);
            List<TemplateClusterInfo> slaveInfos = new ArrayList<>();

            if (indexTemplateResponse.getSlaveInfos() != null && indexTemplateResponse.getSlaveInfos().size() > 0) {
                for (SlaveInfoResponse slaveInfoResponse : indexTemplateResponse.getSlaveInfos()) {
                    TemplateClusterInfo slaveInfo = new TemplateClusterInfo();
                    slaveInfo.setAccessApps(slaveInfoResponse.getAccessApps() == null ? new HashSet<>() : new HashSet<>(slaveInfoResponse.getAccessApps()));
                    slaveInfo.setCluster(slaveInfoResponse.getCluster());
                    slaveInfo.setTopic(slaveInfoResponse.getTopic());
                    slaveInfo.setMappingIndexNameEnable(Objects.isNull(slaveInfoResponse.getMappingIndexNameEnable()) ? false : slaveInfoResponse.getMappingIndexNameEnable());
                    slaveInfo.setTypeIndexMapping(MapUtils.isEmpty(slaveInfoResponse.getTypeIndexMapping()) ? Maps.newHashMap() : slaveInfoResponse.getTypeIndexMapping());

                    slaveInfos.add(slaveInfo);
                }
            }
            indexTemplate.setSlaveInfos(slaveInfos);

            String expression = indexTemplate.getExpression();
            if (expression.endsWith("*")) {
                expression = expression.substring(0, expression.length()-1);
            }

            newIndexTemplateMap.put(expression, indexTemplate);
        }

        bootLogger.info("resetTemplateInfo add indexTemplateMap size={}", response.getData().size());

        indexTemplateMap = newIndexTemplateMap;

        IndexTireBuilder builder = new IndexTireBuilder(indexTemplateMap);
        indexTire = builder.build();

        bootLogger.info("resetTemplateInfo end, newIndexTemplateMap size={}", indexTemplateMap.size());
    }

    private void resetIndexAlias() {
        if (esRestClientService.getESClusterMap() != null) {
            Map<String, String> newIndexToAlias = new HashMap<>();
            for (Map.Entry<String, ESCluster> entry : esRestClientService.getESClusterMap().entrySet()) {
                ESCluster esCluster = entry.getValue();

                try {
                    if (esCluster.getType() == ESCluster.Type.INDEX) {
                        JSONArray alias = getAliasList( esCluster.getEsClient());

                        bootLogger.info("get_alias_list||cluster={}||alias_size={}", esCluster.getCluster(), alias.size());

                        for (int i = 0; i < alias.size(); ++i) {
                            JSONObject item = alias.getJSONObject(i);
                            String strAlias = item.getString("alias");
                            String index = item.getString("index");

                            newIndexToAlias.put(index, strAlias);
                        }
                    }
                } catch (Exception e) {
                    bootLogger.error("cluster={}", esCluster.getCluster(), e);
                }
            }

            this.indexToAlias = newIndexToAlias;
            bootLogger.info("get_alias_list_done||alias_size={}", indexToAlias.size());
        }
    }

    private JSONArray getAliasList(ESClient esClient) throws Exception {
        DirectRequest directRequest = new DirectRequest("GET", "_cat/aliases");
        Map<String, String> params = new HashMap<>();
        params.put("h", "alias,index");
        directRequest.setParams(params);
        directRequest.putHeader("Accept", "application/json");
        if (esClient.getEsVersion().equals(QueryConsts.DEFAULT_ES_VERSION)) {
            directRequest.putHeader("content-type", "application/json");
        }
        DirectResponse directResponse = esClient.direct(directRequest).actionGet();
        if (directResponse.getRestStatus() == RestStatus.OK && directResponse.getResponseContent() != null) {
            try {
                return JSON.parseArray(directResponse.getResponseContent());
            } catch (Exception e) {
                bootLogger.error("get_alias_list_parse_error||cluster={}||content={}", esClient.getClusterName(), directResponse.getResponseContent(), e);
                return new JSONArray();
            }
        } else {
            throw new GatewayException("get client aliases error, status=" + directResponse.getRestStatus());
        }
    }

    private TemplateInfo getAliasesMatch(String indexName, String cluster) {
        Map<String, Map<String, TemplateInfo>> templateAliasesMap = getTemplateAliasMap();

        if (templateAliasesMap.get(cluster) != null) {
            Map<String, TemplateInfo> aliasesMap = templateAliasesMap.get(cluster);
            TemplateInfo templateInfo = aliasesMap.get(indexName);
            if (templateInfo != null) {
                return templateInfo;
            }
        } else {
            for(Map.Entry<String, Map<String, TemplateInfo>> entry : templateAliasesMap.entrySet()) {
                Map<String, TemplateInfo> aliasesMap = entry.getValue();
                TemplateInfo templateInfo = aliasesMap.get(indexName);
                if (templateInfo != null) {
                    return templateInfo;
                }
            }
        }

        return null;
    }

    private TemplateInfo getExpressionMatch(String indexName, String cluster) {
        Map<String, Map<String, TemplateInfo>> templateExpressionMap = getTemplateExpressionMap();

        if (templateExpressionMap.get(cluster) != null) {
            Map<String, TemplateInfo> expressionMap = templateExpressionMap.get(cluster);
            TemplateInfo templateInfo = expressionMap.get(indexName);
            if (templateInfo != null) {
                return templateInfo;
            }

            for (Map.Entry<String, TemplateInfo> entry : expressionMap.entrySet()) {
                String expression = entry.getKey();
                if (expression.endsWith("*")) {
                    if (indexName.startsWith(expression.substring(0, expression.length()-1))) {
                        return entry.getValue();
                    }
                } else if (expression.equals(indexName)) {
                    return entry.getValue();
                }
            }
        } else {
            for(Map.Entry<String, Map<String, TemplateInfo>> entry : templateExpressionMap.entrySet()) {
                Map<String, TemplateInfo> expressionMap = entry.getValue();
                for (Map.Entry<String, TemplateInfo> inEntry : expressionMap.entrySet()) {
                    String expression = inEntry.getKey();
                    if (expression.endsWith("*")) {
                        if (indexName.startsWith(expression.substring(0, expression.length()-1))) {
                            return inEntry.getValue();
                        }
                    } else if (expression.equals(indexName)) {
                        return inEntry.getValue();
                    }
                }
            }
        }

        return null;
    }
}
