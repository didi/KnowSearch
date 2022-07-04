package com.didi.arius.gateway.core.service.arius.impl;

import static com.didi.arius.gateway.common.utils.AppUtil.isAdminAppid;

import java.util.*;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.gateway.GatewayException;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.enums.TemplateBlockTypeEnum;
import com.didi.arius.gateway.common.exception.IndexNotFoundException;
import com.didi.arius.gateway.common.exception.InvalidParameterException;
import com.didi.arius.gateway.common.exception.TemplateBlockException;
import com.didi.arius.gateway.common.exception.TooManyIndexException;
import com.didi.arius.gateway.common.metadata.*;
import com.didi.arius.gateway.common.utils.IndexTire;
import com.didi.arius.gateway.common.utils.IndexTireBuilder;
import com.didi.arius.gateway.common.utils.Regex;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.core.service.arius.AppService;
import com.didi.arius.gateway.core.service.arius.ESClusterService;
import com.didi.arius.gateway.core.service.arius.IndexTemplateService;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectResponse;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.*;
import com.google.common.collect.Maps;

import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class IndexTemplateServiceImpl implements IndexTemplateService {

    private static final Logger bootLogger = LoggerFactory.getLogger( QueryConsts.BOOT_LOGGER);
    private static final Logger logger = LoggerFactory.getLogger(IndexTemplateServiceImpl.class);

    @Autowired
    private AriusAdminRemoteService ariusAdminRemoteService;

    @Autowired
    private AppService appService;

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
        threadPool.submitScheduleAtFixTask(this::resetIndexTemplateInfo, 20, schedulePeriod);
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
        try {
            resetTemplateDetail();
        } catch (Exception e) {
            bootLogger.error("resetTemplateDetail error", e);
        }

        try {
            resetTemplateInfo();
        } catch (Exception e) {
            bootLogger.error("resetTemplateInfo error", e);
        }

        try {
            resetIndexAlias();
        } catch (Exception e) {
            bootLogger.error("resetIndexAlias error", e);
        }

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

            if (oldExpressionMap != null && oldExpressionMap.containsKey(templateInfoResponse.getExpression())) {
                TemplateInfo oldTemplateInfo = oldExpressionMap.get(templateInfoResponse.getExpression());
                templateInfo.setMappings(oldTemplateInfo.getMappings());
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
            indexTemplate.setDeployStatus(IndexTemplate.DeployStatus.integerToStatus(indexTemplateResponse.getBaseInfo().getDeployStatus()));
            indexTemplate.setAliases(indexTemplateResponse.getBaseInfo().getAliases());
            indexTemplate.setIngestPipeline(indexTemplateResponse.getBaseInfo().getIngestPipeline());
            indexTemplate.setBlockRead(null != indexTemplateResponse.getBaseInfo().getBlockRead() && indexTemplateResponse.getBaseInfo().getBlockRead());
            indexTemplate.setBlockWrite(null != indexTemplateResponse.getBaseInfo().getBlockWrite() && indexTemplateResponse.getBaseInfo().getBlockWrite());


            TemplateClusterInfo masterInfo = new TemplateClusterInfo();

            masterInfo.setAccessApps(indexTemplateResponse.getMasterInfo().getAccessApps() == null ? new HashSet<>() : new HashSet<>(indexTemplateResponse.getMasterInfo().getAccessApps()));
            masterInfo.setCluster(indexTemplateResponse.getMasterInfo().getCluster());
            masterInfo.setTopic(indexTemplateResponse.getMasterInfo().getTopic());
            masterInfo.setMappingIndexNameEnable(!Objects.isNull(indexTemplateResponse.getMasterInfo().getMappingIndexNameEnable()) && indexTemplateResponse.getMasterInfo().getMappingIndexNameEnable());
            masterInfo.setTypeIndexMapping( MapUtils.isEmpty(indexTemplateResponse.getMasterInfo().getTypeIndexMapping()) ? Maps.newHashMap() : indexTemplateResponse.getMasterInfo().getTypeIndexMapping());

            indexTemplate.setMasterInfo(masterInfo);
            List<TemplateClusterInfo> slaveInfos = new ArrayList<>();

            dealSlaveInfos(indexTemplateResponse, slaveInfos);
            indexTemplate.setSlaveInfos(slaveInfos);

            String expression = indexTemplate.getExpression();
            if (expression.endsWith("*")) {
                expression = expression.substring(0, expression.length()-1);
            }

            newIndexTemplateMap.put(expression, indexTemplate);

            if(CollectionUtils.isNotEmpty(indexTemplate.getAliases())){
                for(String alias : indexTemplate.getAliases()){
                    newIndexTemplateMap.put(alias, indexTemplate);
                }
            }

        }

        bootLogger.info("resetTemplateInfo add indexTemplateMap size={}", response.getData().size());

        IndexTireBuilder builder = new IndexTireBuilder(indexTemplateMap);
        indexTire = builder.build();
        indexTemplateMap = newIndexTemplateMap;

        String indexTemplateLog = JSON.toJSONString(indexTemplateMap);
        bootLogger.info("resetTemplateInfo end, newIndexTemplateMap size={}, detail={}", indexTemplateMap.size(), indexTemplateLog);
    }

    private void dealSlaveInfos(IndexTemplateResponse indexTemplateResponse, List<TemplateClusterInfo> slaveInfos) {
        if (indexTemplateResponse.getSlaveInfos() != null && !indexTemplateResponse.getSlaveInfos().isEmpty()) {
            for (SlaveInfoResponse slaveInfoResponse : indexTemplateResponse.getSlaveInfos()) {
                TemplateClusterInfo slaveInfo = new TemplateClusterInfo();
                slaveInfo.setAccessApps(slaveInfoResponse.getAccessApps() == null ? new HashSet<>() : new HashSet<>(slaveInfoResponse.getAccessApps()));
                slaveInfo.setCluster(slaveInfoResponse.getCluster());
                slaveInfo.setTopic(slaveInfoResponse.getTopic());
                slaveInfo.setMappingIndexNameEnable(!Objects.isNull(slaveInfoResponse.getMappingIndexNameEnable()) && slaveInfoResponse.getMappingIndexNameEnable());
                slaveInfo.setTypeIndexMapping(MapUtils.isEmpty(slaveInfoResponse.getTypeIndexMapping()) ? Maps.newHashMap() : slaveInfoResponse.getTypeIndexMapping());

                slaveInfos.add(slaveInfo);
            }
        }
    }

    private void resetIndexAlias() {
        if (MapUtils.isEmpty(esRestClientService.getESClusterMap())) {
            return;
        }
        Map<String, String> newIndexToAlias = new HashMap<>();
        for (Map.Entry<String, ESCluster> entry : esRestClientService.getESClusterMap().entrySet()) {
            ESCluster esCluster = entry.getValue();
            if (esCluster.getType() != ESCluster.Type.INDEX) {
                continue;
            }
            try {
                JSONArray alias = getAliasList(esCluster.getEsClient());

                bootLogger.info("get_alias_list||cluster={}||alias_size={}", esCluster.getCluster(), alias.size());

                for (int i = 0; i < alias.size(); ++i) {
                    JSONObject item = alias.getJSONObject(i);
                    String strAlias = item.getString("alias");
                    String index = item.getString("index");

                    newIndexToAlias.put(index, strAlias);
                }
            } catch (Exception e) {
                bootLogger.error("cluster={}", esCluster.getCluster(), e);
            }
        }

        this.indexToAlias = newIndexToAlias;
        bootLogger.info("get_alias_list_done||alias_size={}", indexToAlias.size());
    }

    private JSONArray getAliasList(ESClient esClient) {
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
        Map<String, Map<String, TemplateInfo>> templateAliasesMapInfo = getTemplateAliasMap();

        if (templateAliasesMapInfo.get(cluster) != null) {
            Map<String, TemplateInfo> aliasesMap = templateAliasesMapInfo.get(cluster);
            TemplateInfo templateInfo = aliasesMap.get(indexName);
            return templateInfo;
        } else {
            for(Map.Entry<String, Map<String, TemplateInfo>> entry : templateAliasesMapInfo.entrySet()) {
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
        Map<String, Map<String, TemplateInfo>> templateExpressionMapInfo = getTemplateExpressionMap();

        if (templateExpressionMapInfo.get(cluster) != null) {
            Map<String, TemplateInfo> expressionMap = templateExpressionMapInfo.get(cluster);
            TemplateInfo templateInfo = expressionMap.get(indexName);
            if (templateInfo != null) {
                return templateInfo;
            }
            return getTemplateInfoByIndexName(indexName, expressionMap);
        } else {
            for(Map.Entry<String, Map<String, TemplateInfo>> entry : templateExpressionMapInfo.entrySet()) {
                Map<String, TemplateInfo> expressionMap = entry.getValue();
                TemplateInfo templateInfo = getTemplateInfoByIndexName(indexName, expressionMap);
                if (null != templateInfo) {
                    return templateInfo;
                }
            }
        }

        return null;
    }

    private TemplateInfo getTemplateInfoByIndexName(String indexName, Map<String, TemplateInfo> expressionMap) {
        for (Map.Entry<String, TemplateInfo> entry : expressionMap.entrySet()) {
            String expression = entry.getKey();
            if (expression.endsWith("*")) {
                if (indexName.startsWith(expression.substring(0, expression.length() - 1))) {
                    return entry.getValue();
                }
            } else if (expression.equals(indexName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean addTemplateAlias(int appid, int templateId, String templateName, String aliasName){
        //1、调admin接口增加模板别名
        TemplateAlias templateAlias = new TemplateAlias();
        templateAlias.setLogicId(templateId);
        templateAlias.setName(aliasName);

        TempaletAliasResponse response = ariusAdminRemoteService.addAdminTemplateAlias(templateAlias);
        if (null == response || null == response.getData()) {
            logger.error("addTemplateAlias error, response={}, templateAlias={}", JSON.toJSONString(response),
                    JSON.toJSONString(templateAlias));
            return false;
        } else if (!response.getData()) {
            throw new IllegalArgumentException(response.getMessage());
        }

        AppDetail appDetail = appService.getAppDetail(appid);
        if(null == appDetail){
            return false;
        }

        IndexTemplate indexTemplate = indexTemplateMap.get(templateName);
        if(null == indexTemplate){
            return false;
        }

        //2、更新appDetails信息
        if(null != appDetail.getIndexExp() &&
                appDetail.getIndexExp().contains(templateName)){
            appDetail.getIndexExp().add(aliasName);
        }

        if(null != appDetail.getWindexExp() &&
                appDetail.getWindexExp().contains(templateName)){
            appDetail.getWindexExp().add(aliasName);
        }

        //3、更新indexTemplateMap信息
        if(null == indexTemplate.getAliases()){
            List<String> aliass = new ArrayList<>();
            aliass.add(aliasName);

            indexTemplate.setAliases(aliass);
        }else {
            indexTemplate.getAliases().add(aliasName);
        }

        indexTemplateMap.put(aliasName, indexTemplate);

        return true;
    }

    @Override
    public boolean delTemplateAlias(int appid, int templateId, String templateName, String aliasName){
        //1、调admin接口删除模板别名
        TemplateAlias templateAlias = new TemplateAlias();
        templateAlias.setLogicId(templateId);
        templateAlias.setName(aliasName);

        TempaletAliasResponse response = ariusAdminRemoteService.delAdminTemplateAlias(templateAlias);
		if(null == response || null == response.getData() || !response.getData()){
            logger.error("deleteTemplateAlias error, response={}, templateAlias={}", JSON.toJSONString(response),
                    JSON.toJSONString(templateAlias));
            return false;
		}

        AppDetail appDetail = appService.getAppDetail(appid);
        if(null == appDetail){
            return false;
        }

        IndexTemplate indexTemplate = indexTemplateMap.get(templateName);
        if(null == indexTemplate){
            return false;
        }

        //2、更新appDetails信息
        if(null != appDetail.getIndexExp() &&
                appDetail.getIndexExp().contains(aliasName)){
            appDetail.getIndexExp().remove(aliasName);
        }

        if(null != appDetail.getWindexExp() &&
                appDetail.getWindexExp().contains(aliasName)){
            appDetail.getWindexExp().remove(aliasName);
        }

        //3、更新indexTemplateMap信息
        if(null != indexTemplate.getAliases()){
            indexTemplate.getAliases().remove(aliasName);
        }

        indexTemplateMap.remove(aliasName);

        return true;
    }

    @Override
    public void checkTemplateExist(List<String> indices) {
        if (indices == null || indices.isEmpty()) {
            throw new InvalidParameterException("no index to query");
        }
        for (String index : indices) {
            if (index.startsWith(".")) {
                continue;
            }
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
        }
    }

    @Override
    public void checkTemplateBlock(List<String> indices, AppDetail appDetail, TemplateBlockTypeEnum blockTypeEnum) throws IndexNotFoundException {
        if (indices == null || indices.isEmpty()) {
            throw new InvalidParameterException("no index to query");
        }
        for (String index : indices) {
            if (index.startsWith(".")) {
                continue;
            }
            IndexTemplate indexTemplate = getIndexTemplateByTire(index);
            if (indexTemplate == null) {
                String alias = getIndexAlias(index);
                if (alias != null) {
                    indexTemplate = getIndexTemplateByTire(alias);
                }
            }

            if (indexTemplate == null) {
                throw new IndexNotFoundException(String.format("query can't find index template exception, index=%s", index));
            }

            if (!isAdminAppid(appDetail)) {
                checkBlockType(blockTypeEnum, index, indexTemplate);
            }
        }
    }

    private void checkBlockType(TemplateBlockTypeEnum blockTypeEnum, String index, IndexTemplate indexTemplate) {
        switch (blockTypeEnum) {
            case READ_BLOCK_TYPE:
                if (indexTemplate.getBlockRead()) {
                    throw new TemplateBlockException(String.format("index[%s] block read", index));
                }
                break;
            case WRITE_WRITE_TYPE:
                if (indexTemplate.getBlockWrite()) {
                    throw new TemplateBlockException(String.format("index[%s] block write", index));
                }
                break;
            default:
        }
    }

}
