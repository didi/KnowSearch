package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.DSLSearchUtils;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class IndexCatESDAO extends BaseESDAO {
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
        this.indexName = dataCentreUtil.getAriusCatIndexInfo();
    }

    /**
     * 批量保存索引大小结果
     *
     * @param list
     * @return
     */
    public boolean batchInsert(List<IndexCatCellPO> list) {
        return updateClient.batchInsert(IndexNameUtils.genCurrentDailyIndexName(indexName), typeName, list);
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
     * @param clusters     集群名称列表
     * @param index        索引名称（可选）
     * @param health       健康度（可选）
     * @param from         起始值
     * @param size         每页大小
     * @param sortTerm     排序字段
     * @param orderByDesc  是否降序
     * @return             Tuple<Long, List<IndexCatCellPO>> 命中数 具体数据
     */
    public Tuple<Long, List<IndexCatCellPO>> getCatIndexInfo(List<String> clusters, String index, String health, Integer appId, Integer resourceId,
                                                             Long from,
                                                             Long size, String sortTerm, Boolean orderByDesc) {
        Tuple<Long, List<IndexCatCellPO>> totalHitAndIndexCatCellListTuple;
        String queryTermDsl =  buildQueryTermDsl(clusters, index, health, appId, resourceId);
        String sortType     =  buildSortType(orderByDesc);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CAT_INDEX_INFO_BY_CONDITION,
            queryTermDsl, sortTerm, sortType, from, size);
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
     * @param indexNewStatus    索引标识，true 开启，false 关闭
     * @param retryCount        重试次数
     * @return
     */
    public Boolean batchUpdateCatIndexStatus(String cluster, List<String> indexNameList, boolean indexNewStatus, int retryCount) {
        try {
            return ESOpTimeoutRetry.esRetryExecute("batchUpdateCatIndexStatus", retryCount,
                    () -> updateCatIndexStatus(cluster, indexNameList, indexNewStatus));
        } catch (ESOperateException e) {
            LOGGER.warn("class=IndexCatESDAO||method=batchUpdateCatIndexStatus||errMsg={}", e.getMessage(), e);
        }

        return false;
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
     * @param clusters
     * @param index
     * @param health
     * @return
     */
    private String buildQueryTermDsl(List<String> clusters, String index, String health, Integer appId, Integer resourceId) {
        return "[" + buildTermCell(clusters, index, health, appId, resourceId) +"]";
    }

    private String buildTermCell(List<String> clusters, String index, String health, Integer appId, Integer resourceId) {
        List<String> termCellList = Lists.newArrayList();
        //get cluster dsl term
        termCellList.add(DSLSearchUtils.getTermCellsForExactSearch(clusters, "cluster"));

        //get index dsl term
        termCellList.add(DSLSearchUtils.getTermCellForWildcardSearch(index, "index"));

        //get appId dsl term
        termCellList.add(DSLSearchUtils.getTermCellForExactSearch(appId, "appId"));

        //get resourceId dsl term
        termCellList.add(DSLSearchUtils.getTermCellForExactSearch(resourceId, "resourceId"));

        //get index status term
        if (IndexStatusEnum.isStatusExit(health)) {
            termCellList.add(DSLSearchUtils.getTermCellForExactSearch(health, "health"));
        }

        //get index deleteFlag term
        String deleteFlag = "false";
        termCellList.add(DSLSearchUtils.getTermCellForExactSearch(deleteFlag,"deleteFlag"));
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

    private boolean updateCatIndexStatus(String cluster, List<String> indexNameList, boolean indexNewStatus) {
        List<IndexCatCellPO> indexCatCellPOSList = Lists.newArrayList();
        long currentTimeMillis = System.currentTimeMillis();
        for (String index : indexNameList) {
            IndexCatCellPO indexCatCellPO = new IndexCatCellPO();
            indexCatCellPO.setCluster(cluster);
            indexCatCellPO.setIndex(index);
            indexCatCellPO.setStatus(indexNewStatus ? "open" : "close");
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
}
