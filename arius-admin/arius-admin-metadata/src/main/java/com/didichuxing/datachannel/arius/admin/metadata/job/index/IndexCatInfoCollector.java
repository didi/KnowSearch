package com.didichuxing.datachannel.arius.admin.metadata.job.index;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.shard.Segment;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESShardService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index.IndexCatESDAO;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * @author lyn
 * @date 2021/09/30
 **/
@Component
public class IndexCatInfoCollector extends AbstractMetaDataJob {

    private static final Integer        RETRY_TIMES                = 3;
    @Autowired
    private ClusterPhyService           clusterPhyService;

    @Autowired
    private ESShardService              esShardService;

    @Autowired
    private ESIndexService              esIndexService;

    @Autowired
    private IndexCatESDAO               indexCatESDAO;

    @Autowired
    private IndexTemplatePhyService     indexTemplatePhyService;

    @Autowired
    private ClusterLogicService         clusterLogicService;

    //key: cluster@indexName  value: indexName
    private final Cache<String, Object> notCollectorIndexNameCache = CacheBuilder.newBuilder()
        .expireAfterWrite(60, TimeUnit.MINUTES).maximumSize(10000).build();

    private static final FutureUtil<List<IndexCatCellPO>>    INDEX_CAT_INFO_COLLECTOR_FUTURE_UTIL  = FutureUtil
            .init("IndexCatInfoCollectorFutureUtil", 5, 5, 1000);

    @Override
    public Object handleJobTask(String params) {
        long currentTimeMillis = System.currentTimeMillis();

        List<ClusterPhy> clusterPhyList = clusterPhyService.listAllClusters();
        if (CollectionUtils.isEmpty(clusterPhyList)) { return JOB_SUCCESS;}

        List<String> clusterPhyNameList = clusterPhyList.stream().map(ClusterPhy::getCluster)
            .collect(Collectors.toList());

        // 1. 构建逻辑集群基础信息映射关系
        final Map<Long, String> logicClusterId2NameMap = Maps.newHashMap();
        List<ClusterLogic> logicClusterList = clusterLogicService.listAllClusterLogics();
        if (CollectionUtils.isNotEmpty(logicClusterList)) {
            logicClusterId2NameMap.putAll(ConvertUtil.list2Map(logicClusterList, ClusterLogic::getId, ClusterLogic::getName));
        }

        // 2. 并发采集
        for (String clusterName : clusterPhyNameList) {
                INDEX_CAT_INFO_COLLECTOR_FUTURE_UTIL.callableTask(()-> {
                    List<IndexCatCellPO> indexCatCells = Lists.newArrayList();
                    try {
                        indexCatCells = getIndexCatCells(clusterName, logicClusterId2NameMap);
                    } catch (Exception e) {
                        Thread.currentThread().interrupt();
                        LOGGER.error("class=IndexCatInfoCollector||method=handleJobTask||errMsg={}", e.getMessage(), e);
                    }

                    return indexCatCells;
                });
        }
        List<List<IndexCatCellPO>> lists = INDEX_CAT_INFO_COLLECTOR_FUTURE_UTIL.waitResult();
        List<IndexCatCellPO> res =  Lists.newArrayList();
        for (List<IndexCatCellPO> list : lists) { res.addAll(list);}

        LOGGER.info("class=IndexCatInfoCollector||method=handleJobTask||timeOut={}", System.currentTimeMillis() - currentTimeMillis);

        //延迟2s, 等待 updateNotCollectorIndexNames 方法更新删除标识
        sleep(2000L);

        //TODO: 部署多台admin，这里会出现 过滤失败的问题
        //移除已删除索引, 不采集
        List<IndexCatCellPO> finalSaveIndexCatList = res.stream().filter(this::filterNotCollectorIndexCat)
            .collect(Collectors.toList());
        indexCatESDAO.batchInsert(finalSaveIndexCatList);
        return JOB_SUCCESS;
    }

    public void updateNotCollectorIndexNames(String cluster, List<String> notCollectorIndexNameList) {
        for (String indexName : notCollectorIndexNameList) {
            notCollectorIndexNameCache.put(cluster + "@" + indexName, indexName);
        }
    }

    private List<IndexCatCellPO> getIndexCatCells(String clusterName, Map<Long, String> logicClusterId2NameMap) {
        long timeMillis = System.currentTimeMillis();
        List<CatIndexResult> catIndexResults = esIndexService.syncCatIndex(clusterName, RETRY_TIMES);
        if (CollectionUtils.isEmpty(catIndexResults)) {
            LOGGER.warn("class=IndexCatInfoCollector||method=getIndexInfoFromEs||clusterName={}||index empty",
                clusterName);
            return Lists.newArrayList();
        }

        // 1. 构建segment count 信息
        Map<String, Tuple<Long /*totalSegmentCount*/, Long /*primarySegmentCount*/>> indices2SegmentCountMap =
                getIndex2SegmentCountMap(clusterName);

        // 2. 获取物理集群下所有逻辑模板
        List<IndexTemplatePhyWithLogic> indexTemplatePhyWithLogicList = indexTemplatePhyService
            .getTemplateByPhyCluster(clusterName);

        if (CollectionUtils.isEmpty(indexTemplatePhyWithLogicList)) {
            LOGGER.warn("class=IndexCatInfoCollector||method=getIndexCatInfo||clusterName={}||index template empty",
                clusterName);
        }

        return buildIndexCatCells(catIndexResults, indices2SegmentCountMap, logicClusterId2NameMap, indexTemplatePhyWithLogicList, clusterName,
            timeMillis);
    }

    private Map<String, Tuple<Long /*totalSegmentCount*/, Long /*primarySegmentCount*/>> getIndex2SegmentCountMap(String clusterName) {
        Map<String, Tuple<Long/*totalSegmentCount*/, Long/*primarySegmentCount*/>> index2SegmentCountMap = Maps.newHashMap();

        List<Segment> segments = esShardService.syncGetSegmentsCountInfo(clusterName);
        if (CollectionUtils.isEmpty(segments)) { return index2SegmentCountMap;}

        // 1. 分组统计 key: indexName@p or indexName@r, value: list
        Multimap<String, Segment> index2SegmentMultimap = ConvertUtil.list2MulMap(segments,
                r -> r.getIndex() + "@" + r.getPrimaryFlag(), Segment -> Segment);

        // 2.构建各个Index 的segment tuple
        List<String> indexNamesFromSegments = segments.stream().map(Segment::getIndex).distinct().collect(Collectors.toList());
        for (String indexNameFromSegment : indexNamesFromSegments) {
            Tuple<Long, Long> totalSegmentCount2PrimarySegmentCountTuple = new Tuple<>();
            int primarySegmentCount = index2SegmentMultimap.get(indexNameFromSegment + "@" + "p").size();
            int replicaSegmentCount = index2SegmentMultimap.get(indexNameFromSegment + "@" + "r").size();
            totalSegmentCount2PrimarySegmentCountTuple.setV1((long) (replicaSegmentCount + primarySegmentCount));
            totalSegmentCount2PrimarySegmentCountTuple.setV2((long)primarySegmentCount);

            index2SegmentCountMap.put(indexNameFromSegment, totalSegmentCount2PrimarySegmentCountTuple);
        }

        return index2SegmentCountMap;
    }

    private List<IndexCatCellPO> buildIndexCatCells(List<CatIndexResult> catIndexResults,
                                                    Map<String, Tuple<Long, Long>> indices2SegmentCountMap,
                                                    Map<Long, String> logicClusterId2NameMap,
                                                    List<IndexTemplatePhyWithLogic> indexTemplateList,
                                                    String clusterName, long timeMillis) {
        return  catIndexResults.stream().filter(Objects::nonNull).map(catIndexResult ->
                doBuildIndexCatCell(indices2SegmentCountMap, logicClusterId2NameMap, indexTemplateList, clusterName,
                timeMillis, catIndexResult)).collect(Collectors.toList());
    }

    private IndexCatCellPO doBuildIndexCatCell(Map<String, Tuple<Long, Long>> indices2SegmentCountMap,
                                               Map<Long, String> logicClusterId2NameMap,
                                               List<IndexTemplatePhyWithLogic> indexTemplateList, String clusterName,
                                               long timeMillis, CatIndexResult catIndexResult) {
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
        Optional.ofNullable(indices2SegmentCountMap).map(map -> map.get(catIndexResult.getIndex())).ifPresent(tuple -> {
            indexCatCell.setTotalSegmentCount(tuple.v1());
            indexCatCell.setPrimariesSegmentCount(tuple.v2());
        });

        indexCatCell.setCluster(clusterName);
        indexCatCell.setClusterPhy(clusterName);
        indexCatCell.setTimestamp(timeMillis);
        if (CollectionUtils.isNotEmpty(indexTemplateList)) {
            indexTemplateList.stream().filter(
                indexTemplate -> IndexNameUtils.indexExpMatch(catIndexResult.getIndex(), indexTemplate.getExpression()))
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