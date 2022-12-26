package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_TIMEOUT;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.function.BiFunctionWithESOperateException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.DSLSearchUtils;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ParsingExceptionUtils;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.component.ScrollResultVisitor;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.knowframework.elasticsearch.client.ESClient;
import com.didiglobal.knowframework.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.knowframework.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.hits.ESHit;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.hits.ESHits;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class IndexCatESDAO extends BaseESDAO {
    @Value("${es.update.cluster.name}")
    private String metadataClusterName;
    
    /**
     * 索引名称
     */
    private             String indexName;
    /**
     * type名称
     */
    private             String typeName       = "_doc";
    public static final String SEGMENTS       = "/_segments";
    public static final String INDICES        = "indices";
    public static final String SHARDS         = "shards";
    public static final String SEGMENTS_SHARD = "segments";
    private              String TYPE           = "type";
    private static final String  INDEX = "index";
    private static final String  KEY = "key";
    private static final String  DOC_COUNT = "doc_count";
    private static final Integer AGG_SIZE       = 5000;
    private static final String GROUP_BY_CLUSTER="group_by_cluster";

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusCatIndexInfo();
    }

    /**
     * 批量保存索引大小结果
     *
     * @param list
     * @return
     */
    public boolean batchInsert(List<IndexCatCellPO> list, int retryCount) {
        try {
            return ESOpTimeoutRetry.esRetryExecute("batchInsert", retryCount,
                    () -> updateClient.batchInsert(IndexNameUtils.genCurrentDailyIndexName(indexName), typeName, list));
        } catch (ESOperateException e) {
            LOGGER.error("class=IndexCatESDAO||method=batchInsert||errMsg={}", e.getMessage(), e);
        }
        return false;
    }
    public boolean batchUpsert(List<IndexCatCellPO> list, int retryCount) {
        try {
            return ESOpTimeoutRetry.esRetryExecute("batchInsert", retryCount,
                    () -> updateClient.batchUpdate(IndexNameUtils.genCurrentDailyIndexName(indexName), typeName, list));
        } catch (ESOperateException e) {
            LOGGER.error("class=IndexCatESDAO||method=batchInsert||errMsg={}", e.getMessage(), e);
        }
        return false;
    }


    /**
     * 更新查询模板信息
     *
     * @param list
     * @return
     */
    public boolean updateCatIndexInfo(List<IndexCatCellPO> list) {
        return updateClient.batchUpdate(IndexNameUtils.genCurrentDailyIndexName(indexName), typeName, list);
    }

    /**
     * 根据条件获取CatIndex信息
     *
     * @param index        索引名称（可选）
     * @param health       健康度（可选）
     * @param from         起始值
     * @param size         每页大小
     * @param sortTerm     排序字段
     * @param orderByDesc  是否降序
     * @return             Tuple<Long, List<IndexCatCellPO>> 命中数 具体数据
     */
    public Tuple<Long, List<IndexCatCellPO>> getCatIndexInfo(String cluster, String index, String health, String status,
                                                             Integer projectId, Long from, Long size, String sortTerm,
                                                             Boolean orderByDesc) {
        Tuple<Long, List<IndexCatCellPO>> totalHitAndIndexCatCellListTuple;
        String queryTermDsl = buildQueryTermDsl(cluster, index, health, status, projectId);
        String sortType = buildSortType(orderByDesc);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CAT_INDEX_INFO_BY_CONDITION, queryTermDsl,
            sortTerm, sortType, from, size);
        int retryTime = 3;
        do {
            totalHitAndIndexCatCellListTuple = gatewayClient.performRequestListAndGetTotalCount(metadataClusterName,
                IndexNameUtils.genCurrentDailyIndexName(indexName), typeName, dsl, IndexCatCellPO.class);
        } while (retryTime-- > 0 && null == totalHitAndIndexCatCellListTuple);

        return totalHitAndIndexCatCellListTuple;
    }

    public Tuple<Long, List<IndexCatCellPO>> getIndexListByTerms(String cluster,Integer projectId){
        Tuple<Long, List<IndexCatCellPO>> totalHitAndIndexCatCellListTuple;
        String queryTermDsl = buildQueryTermDsl( cluster,null, null, null, projectId);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_ALL_CAT_INDEX_INFO_BY_TERMS, queryTermDsl);
        int retryTime = 3;
        do {
            totalHitAndIndexCatCellListTuple = gatewayClient.performRequestListAndGetTotalCount(metadataClusterName,
                    IndexNameUtils.genCurrentDailyIndexName(indexName), typeName, dsl, IndexCatCellPO.class);
        } while (retryTime-- > 0 && null == totalHitAndIndexCatCellListTuple);
        return totalHitAndIndexCatCellListTuple;
    }

    /**
     * 更新索引删除标识, 用于真实索引删除后同步删除记录索引信息的元数据索引信息
     *
     * @param cluster           索引所在集群
     * @param indexNameList     索引名称
     * @param retryCount        重试次数
     * @return
     */
    public Boolean batchUpdateCatIndexDeleteFlag(String cluster, List<String> indexNameList, int retryCount) {
        try {
            return ESOpTimeoutRetry.esRetryExecute("batchUpdateCatIndexDeleteFlag", retryCount,
                () -> updateCatIndexDeleteFlag(cluster, indexNameList));
        } catch (ESOperateException e) {
            LOGGER.warn("class=IndexCatESDAO||method=batchUpdateCatIndexDeleteFlag||errMsg={}", e.getMessage(), e);
        }

        return false;
    }

    /**
     * 更新索引开启或关闭标识
     * @param cluster           索引所在集群
     * @param indexNameList     索引名称
     * @param status    索引标识，"open" 开启，"close" 关闭
     * @param retryCount        重试次数
     * @return
     */
    public Boolean batchUpdateCatIndexStatus(String cluster, List<String> indexNameList, String status,
                                             int retryCount) {
        try {
            return ESOpTimeoutRetry.esRetryExecute("batchUpdateCatIndexStatus", retryCount,
                () -> updateCatIndexStatus(cluster, indexNameList, status));
        } catch (ESOperateException e) {
            LOGGER.warn("class=IndexCatESDAO||method=batchUpdateCatIndexStatus||errMsg={}", e.getMessage(), e);
        }

        return false;
    }

    /**
     * 获取不包含模板id并且包含projectId的IndexCatCell信息，作用于平台索引管理新建索引侧
     *
     * @return List<IndexCatCell>
     */
    public  List<IndexCatCell> getPlatformCreateCatIndexList( Integer searchSize) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_PLATFORM_CREATE_CAT_INDEX,searchSize);
   
        // 这里两个时间 用于拿到今天和昨天的数据, 否则无法个获取昨天用户创建的索引数据
        long nowTime = System.currentTimeMillis();
        long oneDayAgo = nowTime - 20 * 60 * 60 * 1000;
        List<IndexCatCell> indexCatCellList = Lists.newCopyOnWriteArrayList();
        String genDailyIndexName = IndexNameUtils.genDailyIndexName(indexName, oneDayAgo, nowTime);
        ScrollResultVisitor<IndexCatCell> scrollResultVisitor = resultList -> {
            if (CollectionUtils.isNotEmpty(resultList)) {
                indexCatCellList.addAll(resultList);
            }
        };
        try {
            ESOpTimeoutRetry.esRetryExecute("getPlatformCreateCatIndexList", 3, () -> {
            
                gatewayClient.queryWithScroll(metadataClusterName, genDailyIndexName, TYPE, dsl, searchSize, null,
                        IndexCatCell.class, scrollResultVisitor);
                return true;
            
            });
        } catch (ESOperateException e) {
            LOGGER.error("class=IndexCatESDAO||method=getPlatformCreateCatIndexList", e);
        }
    
        return indexCatCellList;
    }
    
    public List<IndexCatCellDTO> syncGetSegmentsIndexList(String cluster, Collection<String> indexList)
            throws ESOperateException {
        ESClient esClient = esOpClient.getESClient(cluster);
        if (esClient == null) {
            LOGGER.error("class={}||method=syncGetSegmentsIndexList||clusterName={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster);
            throw new NullPointerException(cluster);
        }
        if (CollectionUtils.isEmpty(indexList)) {
            return Collections.emptyList();
        }
        String uri = String.format("/%s%s", String.join(",", indexList), SEGMENTS);
        DirectRequest directRequest = new DirectRequest(HttpMethod.GET.name(), uri);
        Predicate<DirectResponse> directRequestPredicate = directResponse -> Objects.isNull(directResponse)
                                                                             || RestStatus.OK
                                                                                != directResponse.getRestStatus();
        BiFunctionWithESOperateException<Long, TimeUnit, DirectResponse> directRequestBiFunction = (timeout, unit) -> {
            try {
                return esClient.direct(directRequest).actionGet(timeout, unit);
            } catch (Exception e) {
                LOGGER.error("class=ESIndexDAO||cluster={}||method=syncGetSegmentsIndexList", cluster, e);
                ParsingExceptionUtils.abnormalTermination(e);
            }
            return null;
        };
    
        DirectResponse response = ESOpTimeoutRetry.esRetryExecute("syncGetSegmentsIndexList", 3,
                () -> directRequestBiFunction.apply(Long.valueOf(ES_OPERATE_TIMEOUT), TimeUnit.SECONDS),
                directRequestPredicate
    
        );
        
    
        return Optional.ofNullable(response).filter(r -> RestStatus.OK == r.getRestStatus())
                .map(DirectResponse::getResponseContent).map(JSON::parseObject).map(json -> json.getJSONObject(INDICES))
                .map(i -> buildIndexCatCellDTOList(i, cluster))
            
                .orElse(Lists.newArrayList());
    }
    
    public List<String> syncGetIndexListByProjectIdAndClusterLogic(Integer projectId,
                                                                   String clusterLogic) {
        String realIndexName =IndexNameUtils.genCurrentDailyIndexName(indexName);
        if (Objects.isNull(projectId)) {
            return Collections.emptyList();
        }
        
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_PLATFORM_CREATE_CAT_INDEX_BY_INDEX_PROJECT,
                clusterLogic, projectId, AGG_SIZE);
        return gatewayClient.performRequest(metadataClusterName, realIndexName, TYPE, dsl,
                IndexCatESDAO::buildIndexListByResponse, 3);
    }
     public List<String> syncGetIndexListByProjectIdAndFuzzyIndexAndClusterLogic(Integer projectId, String clusterLogicName,
                                                                                 String index) {
         String realIndexName = IndexNameUtils.genCurrentDailyIndexName(indexName);
         if (Objects.isNull(projectId)) {
             return Collections.emptyList();
         }
    
         String dsl = dslLoaderUtil.getFormatDslByFileName(
                 DslsConstant.GET_PLATFORM_CREATE_CAT_INDEX_BY_INDEX_PROJECT_AND_FUZZY_INDEX_AND_CLUSTER_LOGIC,
                 clusterLogicName, projectId, StringUtils.isNotBlank(index) ? index : "", AGG_SIZE);
         return gatewayClient.performRequest(metadataClusterName, realIndexName, TYPE, dsl,
                 IndexCatESDAO::buildIndexListByResponse, 3);
    }
    
    public List<String> syncGetIndexListByProjectIdAndFuzzyIndexAndClusterPhy( String clusterPhyName,
                                                                              String index) {
         String realIndexName = IndexNameUtils.genCurrentDailyIndexName(indexName);
       
         String dsl = dslLoaderUtil.getFormatDslByFileName(
                 DslsConstant.GET_PLATFORM_CREATE_CAT_INDEX_BY_INDEX_PROJECT_AND_FUZZY_INDEX_AND_CLUSTER_PHY,
                 clusterPhyName, StringUtils.isNotBlank(index) ? index : "", AGG_SIZE);
         return gatewayClient.performRequest(metadataClusterName, realIndexName, TYPE, dsl,
                 IndexCatESDAO::buildIndexListByResponse, 3);
    }
    
    
    
    public Map<String, Integer> syncGetByClusterPhyList(List<String> clusterPhyList) {
        if (CollectionUtils.isEmpty(clusterPhyList)) {
            return Collections.emptyMap();
        }
        String realIndexName = IndexNameUtils.genCurrentDailyIndexName(indexName);
        final String clusterPhyStr = JSON.toJSONString(clusterPhyList);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_PLATFORM_CREATE_CAT_INDEX_GROUP_BY_CLUSTER,
                clusterPhyStr, clusterPhyList.size(), clusterPhyStr);
        return gatewayClient.performRequest(metadataClusterName, realIndexName, TYPE, dsl,
                res -> Optional.ofNullable(res).map(ESQueryResponse::getAggs).map(ESAggrMap::getEsAggrMap)
                        .map(i -> i.get(GROUP_BY_CLUSTER)).map(ESAggr::getBucketList).orElse(Collections.emptyList())
                        .stream().map(ESBucket::getUnusedMap)
                        .map(map -> Tuples.of(map.get(KEY).toString(), ((Integer) map.getOrDefault(DOC_COUNT, 0))))
                        .collect(Collectors.toMap(TupleTwo::v1, TupleTwo::v2))
                
                , 3);
    }
    
    public IndexCatCell syncGetCatIndexInfoById(/* clusterPhy*/String clusterPhy,/*IndexName*/ String index) {
        
        List<String> ids = Collections.singletonList(String.format("%s@%s", clusterPhy, index));
        
        final String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_PLATFORM_CREATE_CAT_INDEX_BY_ID,
                ids.size(), JSON.toJSONString(ids));
    
        Tuple<Long, List<IndexCatCellPO>> tuple = null;
        try {
            tuple = ESOpTimeoutRetry.esRetryExecute("syncGetSegmentsIndexList", 3,
                    () -> gatewayClient.performRequestListAndGetTotalCount(metadataClusterName,
                            IndexNameUtils.genCurrentDailyIndexName(indexName), typeName, dsl, IndexCatCellPO.class),
                    Objects::isNull
        
            );
        } catch (ESOperateException e) {
            LOGGER.error("class={}||cluster={}||method=syncGetCatIndexInfoById", getClass().getSimpleName(), clusterPhy,
                    e);
        }
        
        
        return Optional.ofNullable(tuple).map(Tuple::getV2).map(i -> ConvertUtil.list2List(i, IndexCatCell.class))
                .filter(CollectionUtils::isNotEmpty).orElse(Collections.emptyList()).stream().findFirst().orElse(null);
        
    }

    /**************************************************private******************************************************/
    /**
     * 构建模糊查询dsl语法, 如下
     * {
     * 	"term": {
     * 		"index": {
     * 			"value": "cn_arius.template.label"
     *                } 	}
     * }
     * @param cluster
     * @param index
     * @param health
     * @return
     */
    private String buildQueryTermDsl(String cluster, String index, String health, String status, Integer projectId) {
        return "[" + buildTermCell(cluster, index, health, status, projectId) + "]";
    }

    private String buildTermCell(String cluster, String index, String health, String status, Integer projectId) {
        List<String> termCellList = Lists.newArrayList();
        //projectId == null 时，属于超级项目访问；
        if (null == projectId) {
            //get cluster dsl term
            termCellList.add(DSLSearchUtils.getTermCellForExactSearch(cluster, "cluster"));
        } else {
            //get projectId dsl term
            termCellList.add(DSLSearchUtils.getTermCellForExactSearch(projectId, "projectId"));

            //get resourceId dsl term
            termCellList.add(DSLSearchUtils.getTermCellForExactSearch(cluster, "clusterLogic"));

        }
        //get index dsl term
        termCellList.add(DSLSearchUtils.getTermCellForWildcardSearch(index, "index"));

        //get index status term
        if (IndexStatusEnum.isStatusExit(health)) {
            termCellList.add(DSLSearchUtils.getTermCellForExactSearch(health, "health"));
        }

        //get index status term
        if (IndexStatusEnum.isStatusExit(status)) {
            termCellList.add(DSLSearchUtils.getTermCellForExactSearch(status, "status"));
        }

        //get index deleteFlag term
        String deleteFlag = "false";
        termCellList.add(DSLSearchUtils.getTermCellForExactSearch(deleteFlag, "deleteFlag"));
        return ListUtils.strList2String(termCellList);
    }

    private boolean updateCatIndexDeleteFlag(String cluster, List<String> indexNameList) {
        List<IndexCatCellPO> indexCatCellPOSList = Lists.newArrayList();
        long currentTimeMillis = System.currentTimeMillis();
        for (String index : indexNameList) {
            IndexCatCellPO indexCatCellPO = new IndexCatCellPO();
            indexCatCellPO.setCluster(cluster);
            indexCatCellPO.setIndex(index);
            indexCatCellPO.setDeleteFlag(true);
            indexCatCellPO.setTimestamp(currentTimeMillis);
            indexCatCellPOSList.add(indexCatCellPO);
        }

        return updateCatIndexInfo(indexCatCellPOSList);
    }

    private boolean updateCatIndexStatus(String cluster, List<String> indexNameList, String status) {
        List<IndexCatCellPO> indexCatCellPOSList = Lists.newArrayList();
        long currentTimeMillis = System.currentTimeMillis();
        for (String index : indexNameList) {
            IndexCatCellPO indexCatCellPO = new IndexCatCellPO();
            indexCatCellPO.setCluster(cluster);
            indexCatCellPO.setIndex(index);
            indexCatCellPO.setStatus(status);
            indexCatCellPO.setTimestamp(currentTimeMillis);
            indexCatCellPOSList.add(indexCatCellPO);
        }

        return updateCatIndexInfo(indexCatCellPOSList);
    }
    

    private String buildSortType(Boolean orderByDesc) {
        String sortType = "desc";
        if (orderByDesc == null) {
            return sortType;
        }

        if (orderByDesc) {
            return sortType;
        }

        return "asc";
    }

     private List<IndexCatCellDTO> buildIndexCatCellDTOList(JSONObject jsonObject, String clusterPhy) {
        List<IndexCatCellDTO> indexCatCellDTOList = Lists.newArrayList();
        for (Entry<String, Object> indexJson : jsonObject.entrySet()) {
            String index = indexJson.getKey();
            
            JSONObject value = (JSONObject) indexJson.getValue();
            Long shardSizeIndex = (long) value.getJSONObject(SHARDS).size();
            Long indexShardSegmentsSize = value.getJSONObject(SHARDS).values().stream().filter(Objects::nonNull)
                    .map(JSONArray.class::cast).flatMap(Collection::stream).map(JSONObject.class::cast)
                    .map(json -> json.getJSONObject(SEGMENTS_SHARD)).mapToLong(JSONObject::size).sum();
            IndexCatCellDTO catCellDTO = new IndexCatCellDTO();
            catCellDTO.setIndex(index);
            catCellDTO.setPri(shardSizeIndex);
            catCellDTO.setTotalSegmentCount(indexShardSegmentsSize);
            catCellDTO.setCluster(clusterPhy);
            indexCatCellDTOList.add(catCellDTO);
        }
        return indexCatCellDTOList;
    }
    private static List<String> buildIndexListByResponse(ESQueryResponse response) {
        return Optional.ofNullable(response).map(ESQueryResponse::getHits).map(ESHits::getHits)
                .orElse(Collections.emptyList()).stream().filter(Objects::nonNull).map(ESHit::getSource)
                .filter(Objects::nonNull).map(JSONObject.class::cast).map(jsonObject -> jsonObject.getString(INDEX))
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }



}