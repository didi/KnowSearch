package com.didichuxing.datachannel.arius.admin.metadata.job.index;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index.IndexCatESDAO;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author lyn
 * @date 2021/09/30
 **/
@Component
public class IndexCatInfoCollector extends AbstractMetaDataJob {

    private static final Integer RETRY_TIMES = 3;
    @Autowired
    private ClusterPhyService clusterPhyService;

    @Autowired
    private ESIndexService    esIndexService;

    @Autowired
    private IndexCatESDAO     indexCatESDAO;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private ClusterLogicService clusterLogicService;

    //key: cluster@indexName  value: indexName
    private final Cache<String, Object> notCollectorIndexNameCache = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES).maximumSize(10000).build();
    private Map<Long, String>     logicClusterId2NameMap     = Maps.newConcurrentMap();

    private void initLogicClusterId2NameMap() {
        List<ClusterLogic> logicClusterList = clusterLogicService.listAllClusterLogics();
        if (CollectionUtils.isNotEmpty(logicClusterList)) {
            logicClusterId2NameMap = logicClusterList.stream()
                    .collect(Collectors.toConcurrentMap(ClusterLogic::getId, ClusterLogic::getName, (o1, o2) -> o1));
        }
    }

    @Override
    public Object handleJobTask(String params) {
        List<IndexCatCellPO> indexCatInfoList     =   Lists.newArrayList();
        List<ClusterPhy>     clusterPhyList       =   clusterPhyService.listAllClusters();
        List<String>         clusterPhyNameList   =   clusterPhyList.stream().map(ClusterPhy::getCluster).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(clusterPhyNameList)) {
            return JOB_SUCCESS;
        }
        initLogicClusterId2NameMap();

        long currentTimeMillis = System.currentTimeMillis();
        BatchProcessor.BatchProcessResult<String, List<IndexCatCellPO>> batchResult
                        = new BatchProcessor<String, List<IndexCatCellPO>>()
                        .batchList(clusterPhyNameList)
                        .batchSize(5)
                        .processor(this::getIndexInfoFromEs)
                        .process();
        LOGGER.info("class=IndexCatInfoCollector||method=handleJobTask||timeOut={}", System.currentTimeMillis() - currentTimeMillis);

        if (!batchResult.isSucc() && (batchResult.getErrorMap().size() > 0)) {
            LOGGER.warn(
                "class=IndexCatInfoCollector||method=handleJobTask||clusterList={}||errMsg=batch result error:{}",
                ListUtils.strList2String(clusterPhyNameList), batchResult.getErrorMap());
        }
        
        List<List<IndexCatCellPO>> resultList = batchResult.getResultList();
        for (List<IndexCatCellPO> indexCatCellPOS : resultList) {
            indexCatInfoList.addAll(indexCatCellPOS);
        }

        //延迟2s, 等待 updateNotCollectorIndexNames 方法更新删除标识
        sleep(2000L);
        //移除已删除索引, 不采集
        List<IndexCatCellPO> finalSaveIndexCatList = indexCatInfoList.stream()
            .filter(this::filterNotCollectorIndexCat).collect(Collectors.toList());
        indexCatESDAO.batchInsert(finalSaveIndexCatList);
        return JOB_SUCCESS;
    }

    public void updateNotCollectorIndexNames(String cluster, List<String> notCollectorIndexNameList){
        for (String indexName : notCollectorIndexNameList) {
            notCollectorIndexNameCache.put(cluster + "@" + indexName, indexName);
        }
    }

    public void collectIndexCatInfoByCluster(String cluster) {
        initLogicClusterId2NameMap();
        List<IndexCatCellPO> indexCatInfoList = getIndexCatCells(cluster);
        //移除已删除索引, 不采集
        List<IndexCatCellPO> finalSaveIndexCatList = indexCatInfoList.stream().filter(this::filterNotCollectorIndexCat)
            .collect(Collectors.toList());
        indexCatESDAO.batchInsert(finalSaveIndexCatList);
    }

    public List<IndexCatCellPO> getIndexInfoFromEs(List<String> clusterNameList) {
        List<IndexCatCellPO> catIndexCellList = Lists.newArrayList();
        for (String clusterName : clusterNameList) {
            List<IndexCatCellPO> indexCatCells = getIndexCatCells(clusterName);
            if (null != indexCatCells && !indexCatCells.isEmpty()) {
                catIndexCellList.addAll(indexCatCells);
            }
        }

        return catIndexCellList;
    }

    private List<IndexCatCellPO> getIndexCatCells(String clusterName) {
        long timeMillis                      = System.currentTimeMillis();
        List<CatIndexResult> catIndexResults = esIndexService.syncCatIndex(clusterName, RETRY_TIMES);
        if (CollectionUtils.isEmpty(catIndexResults)) {
            LOGGER.warn("class=IndexCatInfoCollector||method=getIndexInfoFromEs||clusterName={}||index empty",
                    clusterName);
            return Lists.newArrayList();
        }
        Map<String, Tuple<Long, Long>> indicesSegmentCountMap = esIndexService
            .syncGetIndicesSegmentCount(clusterName);

        // 2. 获取物理集群下所有逻辑模板
        List<IndexTemplatePhyWithLogic> indexTemplatePhyWithLogicList = indexTemplatePhyService
            .getTemplateByPhyCluster(clusterName);

        if (CollectionUtils.isEmpty(indexTemplatePhyWithLogicList)) {
            LOGGER.warn("class=IndexCatInfoCollector||method=getIndexCatInfo||clusterName={}||index template empty",
                clusterName);
        }
        
        return buildIndexCatCells(catIndexResults, indicesSegmentCountMap, indexTemplatePhyWithLogicList, clusterName,
            timeMillis);
    }

    private List<IndexCatCellPO> buildIndexCatCells(List<CatIndexResult> catIndexResults, Map<String, Tuple<Long, Long>> indicesSegmentCountMap, List<IndexTemplatePhyWithLogic> indexTemplateList, String clusterName, long timeMillis) {
        List<IndexCatCellPO> indexCatCellList = Lists.newArrayList();
        for (CatIndexResult catIndexResult : catIndexResults) {
            IndexCatCellPO indexCatCell = buildIndexCatCell(indicesSegmentCountMap, indexTemplateList, clusterName, timeMillis, catIndexResult);
            indexCatCellList.add(indexCatCell);
        }
        return indexCatCellList;
    }

    private IndexCatCellPO buildIndexCatCell(Map<String, Tuple<Long, Long>> indicesSegmentCountMap, List<IndexTemplatePhyWithLogic> indexTemplateList, String clusterName, long timeMillis, CatIndexResult catIndexResult) {
        IndexCatCellPO indexCatCell = new IndexCatCellPO();
        if (!AriusObjUtils.isBlack(catIndexResult.getStoreSize())) {
            indexCatCell.setStoreSize(SizeUtil.getUnitSize(catIndexResult.getStoreSize()));
        }
        if (!AriusObjUtils.isBlack(catIndexResult.getPriStoreSize())) {
            indexCatCell.setPriStoreSize(SizeUtil.getUnitSize(catIndexResult.getPriStoreSize()));
        }
        if (!AriusObjUtils.isBlack(catIndexResult.getDocsCount())) {
            indexCatCell.setDocsCount(Long.parseLong(catIndexResult.getDocsCount()));
        }
        if (!AriusObjUtils.isBlack(catIndexResult.getDocsDeleted())) {
            indexCatCell.setDocsDeleted(Long.parseLong(catIndexResult.getDocsDeleted()));
        }
        if (!AriusObjUtils.isBlack(catIndexResult.getRep())) {
            indexCatCell.setRep(Long.parseLong(catIndexResult.getRep()));
        }
        if (!AriusObjUtils.isBlack(catIndexResult.getPri())) {
            indexCatCell.setPri(Long.parseLong(catIndexResult.getPri()));
        }
        if (!AriusObjUtils.isBlack(catIndexResult.getStatus())) {
            indexCatCell.setStatus(catIndexResult.getStatus());
        }
        if (!AriusObjUtils.isBlack(catIndexResult.getHealth())) {
            indexCatCell.setHealth(catIndexResult.getHealth());
        }
        if (!AriusObjUtils.isBlack(catIndexResult.getIndex())) {
            indexCatCell.setIndex(catIndexResult.getIndex());
        }
        Optional.ofNullable(indicesSegmentCountMap).map(map -> map.get(catIndexResult.getIndex()))
                .ifPresent(tuple -> {
                    indexCatCell.setTotalSegmentCount(tuple.v1());
                    indexCatCell.setPrimariesSegmentCount(tuple.v2());
                });

        indexCatCell.setCluster(clusterName);
        indexCatCell.setClusterPhy(clusterName);
        indexCatCell.setTimestamp(timeMillis);
        if (CollectionUtils.isNotEmpty(indexTemplateList)) {
            indexTemplateList.stream()
                    .filter(indexTemplate -> IndexNameUtils.indexExpMatch(catIndexResult.getIndex(),
                            indexTemplate.getExpression()))
                    .map(IndexTemplatePhyWithLogic::getLogicTemplate).forEach(indexTemplate -> {
                        String clusterLogic = logicClusterId2NameMap.get(indexTemplate.getResourceId());
                        indexCatCell.setClusterLogic(clusterLogic);
                        indexCatCell.setResourceId(indexTemplate.getResourceId());
                        indexCatCell.setTemplateId(indexTemplate.getId());
                        indexCatCell.setProjectId(indexTemplate.getProjectId());
                    });
        }
        return indexCatCell;
    }

    private boolean filterNotCollectorIndexCat(IndexCatCellPO indexCatCellPO) {
        return notCollectorIndexNameCache.getIfPresent(indexCatCellPO.getKey()) == null;
    }

    private void sleep(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}