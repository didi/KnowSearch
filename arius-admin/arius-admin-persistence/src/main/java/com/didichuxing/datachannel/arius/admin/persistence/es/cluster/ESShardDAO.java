package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.po.shard.ShardCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NullESClientException;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.knowframework.elasticsearch.client.ESClient;
import com.didiglobal.knowframework.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.knowframework.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandMethodsEnum.SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandMethodsEnum.SHARD_ASSIGNMENT;

/**
 * Created by linyunan on 3/22/22
 */
@Repository
public class ESShardDAO extends BaseESDAO {

    @Value("${es.update.cluster.name}")
    private String              metadataClusterName;

    @Autowired
    private ESIndexDAO indexDAO;

    /**
     * 索引名称
     */
    private String              indexName;
    /**
     * type名称
     */
    private String              typeName                   = "type";
    private String              shard                      = "shard";
    private String              store                      = "store";
    private String              docs                       = "docs";
    private String              index                      = "index";
    private String              ip                         = "ip";
    private String              node                       = "node";
    private String              prirep                     = "prirep";
    private String              state                      = "state";
    private String              CLOSED                     = "closed";
    private String              OPEN                       = "open";

    private static final FutureUtil<List<ShardCatCellPO>> CAT_SHARD_FUTURE = FutureUtil.init("CAT_SHARD_FUTURE", 10, 10, 100);


    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusCatShardInfo();
    }

    public List<ShardCatCellPO> catShard(String clusterName) throws ESOperateException {
        ESClient client = esOpClient.getESClient(clusterName);
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=catShard||clusterName={}||errMsg=esClient is null", clusterName);
            throw new NullESClientException(clusterName);
        }
        try {
            return getShardCatCellPOS(clusterName, client, SHARD.getUri());
        } catch (Exception e) {
            final String exception = ParsingExceptionUtils.getESErrorMessageByException(
                    e);
            if (StringUtils.equals(exception,CLOSED)){
                return getLowerVersionShardCatCellPOList(clusterName);
            }
            LOGGER.warn("class=ESClusterDAO||method=catShard||cluster={}||mg=get es segments fail", clusterName, e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return new ArrayList<>();
    }

    /**
     *  获取低版本的shard分布数据
     * @param cluster 物理集群
     * @return
     */
    private List<ShardCatCellPO> getLowerVersionShardCatCellPOList(String cluster) throws ESOperateException {

        List<ShardCatCellPO> shardCatCellPOS = Lists.newArrayList();
        ESClient client = fetchESClientByCluster(cluster);
        if (client == null) {
            throw new NullESClientException(cluster);
        }
        try {
            List<CatIndexResult> catIndexResults = indexDAO.catIndices(cluster);
            List<String> openIndexNames = catIndexResults.stream().filter(index->StringUtils.equals(OPEN,index.getStatus()))
                    .map(CatIndexResult::getIndex).collect(Collectors.toList());
            //如果索引特别多，需要分批构造uri进行处理
            List<List<String>> openIndexNamesList = org.apache.commons.collections4.ListUtils.partition(openIndexNames, 50);
            for(List<String> openIndexNamePartition:openIndexNamesList){
                String uri = SHARD.getUri() + "/" + String.join(",", openIndexNamePartition);
                CAT_SHARD_FUTURE.callableTask(()->getShardCatCellPOS(cluster, client, uri));
            }
            CAT_SHARD_FUTURE.waitResult().forEach(catCellList->shardCatCellPOS.addAll(catCellList));
            return shardCatCellPOS;
        } catch (Exception e) {
            LOGGER.error("class=ESShardDao||method=getLowerVersionShardCatCellPOList||clusterName={}", cluster,
                    e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return Lists.newArrayList();
    }

    /**
     * 获取集群下的shard分布信息
     * @param cluster 物理集群
     * @param client client
     * @param uri uri
     * @return
     */
    private List<ShardCatCellPO> getShardCatCellPOS(String cluster, ESClient client, String uri) throws ESOperateException {
        try{
            DirectRequest directRequest = new DirectRequest(SHARD.getMethod(), uri);
            DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                    && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                return buildShardCatCellPOs(directResponse.getResponseContent(), cluster);
            }
        } catch (Exception e) {
            LOGGER.error("class=ESShardDAO||method=getShardCatCellPOS||clusterName={}||uri={}", cluster,uri,
                    e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return Collections.emptyList();
    }

    /**
     * 通过集群名称获取对应的ES Client
     * @param clusterName 集群名称
     * @return
     */
    private ESClient fetchESClientByCluster(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        if (client == null) {
            LOGGER.warn("class=ESIndexDAO||method=fetchESClientByCluster||cannot get es client,cluster={}",
                    clusterName);
        }
        return client;
    }

    private List<ShardCatCellPO> buildShardCatCellPOs(String responseContent, String clusterName) {
        List<ShardCatCellPO> shardCatCellPOList = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(responseContent);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject shardInfo = jsonArray.getJSONObject(i);
            String shardStr = shardInfo.getString(shard);
            String storeStr = shardInfo.getString(store);
            String docsStr = shardInfo.getString(docs);

            ShardCatCellPO shardCatCellPO = new ShardCatCellPO();
            shardCatCellPO.setClusterPhy(clusterName);
            shardCatCellPO.setShard(shardStr);
            shardCatCellPO.setStore(storeStr);
            shardCatCellPO.setDocs(docsStr);
            shardCatCellPO.setIndex(shardInfo.getString(index));
            shardCatCellPO.setIp(shardInfo.getString(ip));
            shardCatCellPO.setNode(shardInfo.getString(node));
            shardCatCellPO.setPrirep(shardInfo.getString(prirep));
            shardCatCellPO.setState(shardInfo.getString(state));
            shardCatCellPOList.add(shardCatCellPO);
        }
        return shardCatCellPOList;
    }
    
    /**
     * shard分配说明
     * @param clusterPhyName 物理集群名称
     * @return
     */
    public String shardAssignment(String clusterPhyName) throws ESOperateException {
        ESClient client = esOpClient.getESClient(clusterPhyName);
        String result = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=shardAssignment||clusterName={}||errMsg=esClient is null", clusterPhyName);
            throw new NullESClientException(clusterPhyName);
        }
        try {
            DirectRequest directRequest = new DirectRequest(SHARD_ASSIGNMENT.getMethod(), SHARD_ASSIGNMENT.getUri());
            DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                    && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                result = directResponse.getResponseContent();
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("class=ESClusterDAO||method=shardAssignment||msg=get es segments faill||cluster={}", clusterPhyName,e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return null;
    }

    /**
     * 根据条件获取Catshard信息
     *
     * @param from         起始值
     * @param size         每页大小
     * @param sortTerm     排序字段
     * @param orderByDesc  是否降序
     * @return             Tuple<Long, List<IndexCatCellPO>> 命中数 具体数据
     */
    public Tuple<Long, List<ShardCatCellPO>> getCatShardInfo(String cluster, Integer projectId,String keyword,
                                                             Long from,
                                                             Long size, String sortTerm, Boolean orderByDesc) {
        Tuple<Long, List<ShardCatCellPO>> totalHitAndIndexCatCellListTuple;
        String queryTermDsl =  buildQueryTermDsl(cluster,projectId,keyword);
        String sortType     =  buildSortType(orderByDesc);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CAT_SHARD_INFO_BY_CONDITION,
                queryTermDsl, sortTerm, sortType, from, size);
        int retryTime = 3;
        do {
            totalHitAndIndexCatCellListTuple = gatewayClient.performRequestListAndGetTotalCount(metadataClusterName,
                    IndexNameUtils.genCurrentDailyIndexName(indexName), typeName, dsl, ShardCatCellPO.class);
        } while (retryTime-- > 0 && null == totalHitAndIndexCatCellListTuple);

        return totalHitAndIndexCatCellListTuple;
    }

    /**
     * 批量保存索引大小结果
     *
     * @param list
     * @return
     */
    public boolean batchInsert(List<ShardCatCellPO> list) {
        return updateClient.batchInsert(IndexNameUtils.genCurrentDailyIndexName(indexName), typeName, list);
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
     * @return
     */
    private String buildQueryTermDsl(String cluster,  Integer projectId,String keyword) {
        return "[" + buildTermCell(cluster, projectId,keyword) +"]";
    }
    private String buildTermCell(String cluster, Integer projectId, String keyword) {
        List<String> termCellList = Lists.newArrayList();
        //projectId == null 时，属于超级项目访问；
        if (null == projectId) {
            //get cluster dsl term
            termCellList.add(DSLSearchUtils.getTermCellForExactSearch(cluster, "clusterPhy"));
        } else {
            //get projectId dsl term
            termCellList.add(DSLSearchUtils.getTermCellForExactSearch(projectId, "projectId"));

            //get resourceId dsl term
            termCellList.add(DSLSearchUtils.getTermCellForExactSearch(cluster, "clusterLogic"));
        }
        termCellList.add(buildShouldTermCell(keyword));
        return ListUtils.strList2String(termCellList);
    }

    /**
     * 构造多字段模糊查询
     * {
     *           "bool": {
     *             "should": [
     *               {
     *                 "wildcard": {
     *                   "ip": {
     *                     "value": "*lyn-ks*"
     *                   }
     *                 }
     *               },{
     *                 "wildcard": {
     *                   "index": {
     *                     "value": "*lyn-ks*"
     *                   }
     *                 }
     *               }
     *             ]
     *           }
     *         }
     * @param keyword 关键字
     * @return
     */
    private String buildShouldTermCell(String keyword){
        //构造should的条件
        List<String> shouldCellList = Lists.newArrayList();
        if (StringUtils.isNotBlank(keyword)){
            shouldCellList.add(DSLSearchUtils.getTermCellForWildcardSearch(keyword, "index"));
            shouldCellList.add(DSLSearchUtils.getTermCellForWildcardSearch(keyword, "node"));
            shouldCellList.add(DSLSearchUtils.getTermCellForWildcardSearch(keyword, "ip"));
            shouldCellList.add(DSLSearchUtils.getTermCellForWildcardSearch(keyword, "state"));
        }

        return dslLoaderUtil.getFormatDslByFileName(DslsConstant.SHOULD_TERM_CELL,
                ListUtils.strList2String(shouldCellList));
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
}