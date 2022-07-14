package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import com.alibaba.fastjson.JSONArray;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.po.shard.ShardCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.util.DSLSearchUtils;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandMethodsEnum.SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandMethodsEnum.SHARD_ASSIGNMENT;

/**
 * Created by linyunan on 3/22/22
 */
@Repository
public class ESShardDAO extends BaseESDAO {
    @Value("${es.update.cluster.name}")
    private String              metadataClusterName;
    /**
     * 索引名称
     */
    private String              indexName;
    /**
     * type名称
     */
    private String              typeName                   = "type";

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusCatShardInfo();
    }

    public List<ShardCatCellPO> catShard(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        List<ShardCatCellPO> ecSegmentsOnIps = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=catShard||clusterName={}||errMsg=esClient is null", clusterName);
            return new ArrayList<>();
        }
        try {
            DirectRequest directRequest = new DirectRequest(SHARD.getMethod(), SHARD.getUri());
            DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                    && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                ecSegmentsOnIps = JSONArray.parseArray(directResponse.getResponseContent(), ShardCatCellPO.class);
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=catShard||cluster={}||mg=get es segments fail", clusterName, e);
            return new ArrayList<>();
        }
        return ecSegmentsOnIps;
    }

    /**
     * shard分配说明
     * @param clusterPhyName 物理集群名称
     * @return
     */
    public String shardAssignment(String clusterPhyName) {
        ESClient client = esOpClient.getESClient(clusterPhyName);
        String result = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=shardAssignment||clusterName={}||errMsg=esClient is null", clusterPhyName);
            return null;
        }
        try {
            DirectRequest directRequest = new DirectRequest(SHARD_ASSIGNMENT.getMethod(), SHARD_ASSIGNMENT.getUri());
            DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                    && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                result = directResponse.getResponseContent();
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=shardAssignment||cluster={}||mg=get es segments fail", clusterPhyName, e);
            return null;
        }
        return result;
    }

    /**
     * 根据条件获取CatIndex信息
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

    private String buildTermCell(String cluster, Integer projectId,String keyword) {
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
        if (StringUtils.isNotBlank(keyword)){
            termCellList.add(DSLSearchUtils.getTermCellForWildcardSearch(keyword, "clusterPhy"));
        }
        return ListUtils.strList2String(termCellList);
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