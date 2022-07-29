package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
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
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class IndexCatESDAO extends BaseESDAO {
    @Value("${es.update.cluster.name}")
    private String metadataClusterName;

    /**
     * 索引名称
     */
    private String indexName;
    /**
     * type名称
     */
    private String typeName = "_doc";

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
        String queryTermDsl = buildQueryTermDsl(cluster,null, index, health, status, projectId);
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

    public Tuple<Long, List<IndexCatCellPO>> getIndexListByTerms(String clusterLogicName){
        Tuple<Long, List<IndexCatCellPO>> totalHitAndIndexCatCellListTuple;
        String queryTermDsl = buildQueryTermDsl(null, clusterLogicName,null, null, null, null);
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
     * @return          List<IndexCatCell>
     */
    public List<IndexCatCell> syncGetPlatformCreateCatExistsHealthIndexList() {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_PLATFORM_CREATE_CAT_INDEX_EXISTS_HEALTH);
        int retryTime = 3;
        List<IndexCatCell> indexCatCell;
        // 这里两个时间 用于拿到今天和昨天的数据, 否则无法个获取昨天用户创建的索引数据
        long nowTime = System.currentTimeMillis();
        long oneDayAgo = nowTime - 20 * 60 * 60 * 1000;
        do {
            indexCatCell = gatewayClient.performRequest(metadataClusterName,
                    IndexNameUtils.genDailyIndexName(indexName, oneDayAgo, nowTime), typeName, dsl, IndexCatCell.class);
        } while (retryTime-- > 0 && CollectionUtils.isEmpty(indexCatCell));

        return indexCatCell;
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
    private String buildQueryTermDsl(String cluster,String clusterLogic, String index, String health, String status, Integer projectId) {
        return "[" + buildTermCell(cluster,clusterLogic, index, health, status, projectId) + "]";
    }

    private String buildTermCell(String cluster,String clusterLogic, String index, String health, String status, Integer projectId) {
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
        if (StringUtils.isNotBlank(clusterLogic)){
            termCellList.add(DSLSearchUtils.getTermCellForExactSearch(clusterLogic, "clusterLogic"));
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


}