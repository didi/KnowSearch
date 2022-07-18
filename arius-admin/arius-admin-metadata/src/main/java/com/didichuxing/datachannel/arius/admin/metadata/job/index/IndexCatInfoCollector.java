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
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.shard.Segment;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESShardService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
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
    private ESIndexCatService           esIndexCatService;

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

        // 1. 构建逻辑集群基础信息映射关系, 用于后面做基础数据构建
        final Map<Long, String> logicClusterId2NameMap = Maps.newHashMap();
        List<ClusterLogic> logicClusterList = clusterLogicService.listAllClusterLogics();
        if (CollectionUtils.isNotEmpty(logicClusterList)) {
            logicClusterId2NameMap.putAll(ConvertUtil.list2Map(logicClusterList, ClusterLogic::getId, ClusterLogic::getName));
        }

        // 2. 获取通过平台索引管理 创建的索引cat_index信息，其中不包含templateId，但包含projectId
        List<IndexCatCell> indexCatCellList = esIndexCatService.syncGetHasProjectIdButNotTemplateIdCatIndexList();
        // 这里的cluster 用户侧创建为逻辑集群名称，运维侧创建为物理集群名称
        Map<String/*cluster@index*/, IndexCatCell> index2IndexCatCellMap = ConvertUtil.list2Map(indexCatCellList,
                IndexCatCell::getKey, r -> r);

        // 3. 并发采集
        for (String clusterName : clusterPhyNameList) {
                INDEX_CAT_INFO_COLLECTOR_FUTURE_UTIL.callableTask(()-> {
                    List<IndexCatCellPO> indexCatCells = Lists.newArrayList();
                    try {
                        // 0. 从es中获取数据
                        List<CatIndexResult> catIndexResults = esIndexService.syncCatIndex(clusterName, RETRY_TIMES);
                        if (CollectionUtils.isEmpty(catIndexResults)) {
                            LOGGER.warn("class=IndexCatInfoCollector||method=getIndexInfoFromEs||clusterName={}||index empty", clusterName);
                            return indexCatCells;
                        }

                        long timeMillis = System.currentTimeMillis();

                        // 1. 构建segment count 信息
                        Map<String, Tuple<Long /*totalSegmentCount*/, Long /*primarySegmentCount*/>> indices2SegmentCountMap =
                                getIndex2SegmentCountMap(clusterName);

                        // 2. 获取物理集群下所有逻辑模板
                        List<IndexTemplatePhyWithLogic> indexTemplatePhyWithLogicList = indexTemplatePhyService.getTemplateByPhyCluster(clusterName);

                        if (CollectionUtils.isEmpty(indexTemplatePhyWithLogicList)) {
                            LOGGER.warn("class=IndexCatInfoCollector||method=getIndexCatInfo||clusterName={}||index template empty",
                                    clusterName);
                        }

                        Map<String/*templateName*/, IndexTemplatePhyWithLogic> templateName2IndexTemplatePhyWithLogicMap =
                                ConvertUtil.list2Map(indexTemplatePhyWithLogicList, IndexTemplatePhyWithLogic::getName, r -> r);

                        // 3.1 获取匹配平台模板的cat_index信息
                        List<CatIndexResult> catIndexMatchAriusTemplateList = catIndexResults.stream()
                                .filter(r -> templateName2IndexTemplatePhyWithLogicMap.containsKey(
                                        TemplateUtils.getMatchTemplateNameByIndexName(r.getIndex())))
                                .collect(Collectors.toList());

                        // 3.2 根据模板信息构建Arius平台索引cat_index元数据信息
                        List<IndexCatCellPO> ariusIndexCatCells = buildAriusIndexCatCells(catIndexMatchAriusTemplateList,
                                logicClusterId2NameMap,
                                indices2SegmentCountMap,
                                templateName2IndexTemplatePhyWithLogicMap,
                                clusterName,
                                timeMillis);
                        indexCatCells.addAll(ariusIndexCatCells);

                        // 4.1 获取不匹配平台模板cat_index列表（通过索引管理创建，或者其他第三方客户端创建）
                        List<CatIndexResult> catIndexMatchNativeTemplateList = catIndexResults.stream()
                                .filter(r -> !catIndexMatchAriusTemplateList.contains(r))
                                .collect(Collectors.toList());

                        // 4.3 无需模板信息构建原生索引cat_index元数据信息
                        List<IndexCatCellPO> nativeIndexCatCells = buildNativeIndexCatCells(catIndexMatchNativeTemplateList,
                                indices2SegmentCountMap,
                                index2IndexCatCellMap,
                                clusterName,
                                timeMillis);
                        indexCatCells.addAll(nativeIndexCatCells);
                    } catch (Exception e) {
                        Thread.currentThread().interrupt();
                        LOGGER.error("class=IndexCatInfoCollector||method=handleJobTask||errMsg={}", e.getMessage(), e);
                    }

                    return indexCatCells;
                });
        }

        List<List<IndexCatCellPO>> lists = INDEX_CAT_INFO_COLLECTOR_FUTURE_UTIL.waitResult();
        List<IndexCatCellDTO> res =  Lists.newArrayList();
        for (List<IndexCatCellPO> list : lists) { res.addAll(ConvertUtil.list2List(list, IndexCatCellDTO.class));}

        LOGGER.info("class=IndexCatInfoCollector||method=handleJobTask||timeOut={}", System.currentTimeMillis() - currentTimeMillis);

        //延迟2s, 等待 updateNotCollectorIndexNames 方法更新删除标识
        sleep(2000L);

        //TODO: 部署多台admin，这里会出现过滤失败的问题
        //移除已删除索引, 不采集
        List<IndexCatCellDTO> finalSaveIndexCatList = res.stream().filter(this::filterNotCollectorIndexCat)
            .collect(Collectors.toList());
        esIndexCatService.syncInsertCatIndex(finalSaveIndexCatList, RETRY_TIMES);
        return JOB_SUCCESS;
    }

    public void updateNotCollectorIndexNames(String cluster, List<String> notCollectorIndexNameList) {
        for (String indexName : notCollectorIndexNameList) {
            notCollectorIndexNameCache.put(cluster + "@" + indexName, indexName);
        }
    }

    /**
     * 构建与平台匹配的索引Cat_index信息
     * @param catIndexMatchAriusTemplateList                匹配模板的CatIndex列表
     * @param logicClusterId2NameMap                        logicClusterId2NameMap
     * @param indices2SegmentCountMap                       indices2SegmentCountMap
     * @param templateName2IndexTemplatePhyWithLogicMap     templateName2IndexTemplatePhyWithLogicMap
     * @param clusterName
     * @param timeMillis
     * @return
     */
    private List<IndexCatCellPO> buildAriusIndexCatCells(List<CatIndexResult> catIndexMatchAriusTemplateList,
                                                         Map<Long, String> logicClusterId2NameMap,
                                                         Map<String, Tuple<Long /*totalSegmentCount*/, Long /*primarySegmentCount*/>> indices2SegmentCountMap,
                                                         Map<String/*templateName*/, IndexTemplatePhyWithLogic> templateName2IndexTemplatePhyWithLogicMap,
                                                         String clusterName,
                                                         long timeMillis) {
        List<IndexCatCellPO> res = Lists.newArrayList();
        for (CatIndexResult catIndexResult : catIndexMatchAriusTemplateList) {
            IndexCatCellPO indexCatCellPO = new IndexCatCellPO();

            buildBasicIndexCatInfo(indexCatCellPO, indices2SegmentCountMap, clusterName, timeMillis, catIndexResult);

            // 根据索引名称获取平台模板名称, 匹配为null，则不去设置设置模板相关的属性
            String templateName = TemplateUtils.getMatchTemplateNameByIndexName(indexCatCellPO.getIndex());
            if (null != templateName && null != templateName2IndexTemplatePhyWithLogicMap.get(templateName)) {
                IndexTemplatePhyWithLogic indexTemplatePhyWithLogic = templateName2IndexTemplatePhyWithLogicMap.get(templateName);

                IndexTemplate logicTemplate = indexTemplatePhyWithLogic.getLogicTemplate();
                if (null != logicTemplate) {
                    String clusterLogic = logicClusterId2NameMap.get(logicTemplate.getResourceId());
                    indexCatCellPO.setClusterLogic(clusterLogic);
                    indexCatCellPO.setResourceId(logicTemplate.getResourceId());
                    indexCatCellPO.setTemplateId(logicTemplate.getId());
                    indexCatCellPO.setProjectId(logicTemplate.getProjectId());
                }
            }
            res.add(indexCatCellPO);

        }
        return res;
    }

    /**
     * 构建原生索引Cat_index信息
     * @param catIndexMatchNativeTemplateList
     * @param indices2SegmentCountMap
     * @param index2IndexCatCellMap
     * @param clusterName
     * @param timeMillis
     * @return                                    List<IndexCatCellPO>
     */
    private List<IndexCatCellPO> buildNativeIndexCatCells(List<CatIndexResult> catIndexMatchNativeTemplateList,
                                                          Map<String, Tuple<Long /*totalSegmentCount*/, Long /*primarySegmentCount*/>>
                                                                  indices2SegmentCountMap,
                                                          Map<String/*cluster@index*/, IndexCatCell> index2IndexCatCellMap,
                                                          String clusterName,
                                                          long timeMillis) {
        List<IndexCatCellPO> res = Lists.newArrayList();

        for (CatIndexResult catIndexResult : catIndexMatchNativeTemplateList) {
            IndexCatCellPO indexCatCellPO = new IndexCatCellPO();
            // 构建基础数据
            buildBasicIndexCatInfo(indexCatCellPO, indices2SegmentCountMap, clusterName, timeMillis, catIndexResult);

            // 索引管理所创建的索引需要构建以下平台相关信息（项目、物理集群、逻辑集群等）
            if (index2IndexCatCellMap.containsKey(indexCatCellPO.getKey())) {
                IndexCatCell indexCatCell = index2IndexCatCellMap.get(indexCatCellPO.getKey());
                indexCatCellPO.setProjectId(indexCatCell.getProjectId());
                indexCatCellPO.setCluster(indexCatCell.getClusterLogic());
                indexCatCellPO.setClusterPhy(indexCatCell.getClusterPhy());
                indexCatCellPO.setClusterLogic(indexCatCell.getClusterLogic());
                indexCatCellPO.setResourceId(indexCatCell.getResourceId());
            }

            res.add(indexCatCellPO);
        }
        return res;
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

    /**
     * 构建基础数据
     * @param indexCatCell                   需要构建 indexCatCell
     * @param indices2SegmentCountMap
     * @param clusterName
     * @param timeMillis
     * @param catIndexResult
     */
    private void buildBasicIndexCatInfo(IndexCatCellPO indexCatCell,
                                        Map<String, Tuple<Long, Long>> indices2SegmentCountMap,
                                        String clusterName,
                                        long timeMillis,
                                        CatIndexResult catIndexResult) {

        if (!AriusObjUtils.isBlack(catIndexResult.getIndex())) {
            indexCatCell.setIndex(catIndexResult.getIndex());
        }
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

        Optional.ofNullable(indices2SegmentCountMap).map(map -> map.get(catIndexResult.getIndex())).ifPresent(tuple -> {
            indexCatCell.setTotalSegmentCount(tuple.v1());
            indexCatCell.setPrimariesSegmentCount(tuple.v2());
        });

        indexCatCell.setCluster(clusterName);
        indexCatCell.setClusterPhy(clusterName);
        indexCatCell.setDeleteFlag(false);
        indexCatCell.setTimestamp(timeMillis);
    }

    private boolean filterNotCollectorIndexCat(IndexCatCellDTO indexCatCellDTO) {
        return notCollectorIndexNameCache.getIfPresent(indexCatCellDTO.getKey()) == null;
    }

    private void sleep(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}