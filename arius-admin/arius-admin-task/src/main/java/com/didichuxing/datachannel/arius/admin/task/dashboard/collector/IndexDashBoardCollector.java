package com.didichuxing.datachannel.arius.admin.task.dashboard.collector;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.BYTE_TO_MB;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESIndicesStatsService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.ShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.shard.Segment;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.DashBoardStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.IndexMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.MappingConfigUtil;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESShardService;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created by linyunan on 3/11/22
 * dashboard单个集群索引采集器
 */
@Component
public class IndexDashBoardCollector extends BaseDashboardCollector {
    private static final ILog   LOGGER     = LogFactory.getLog(IndexDashBoardCollector.class);
    private static final String NO_REPLICA = "0";

    @Autowired
    private ESShardService          esShardService;

    @Autowired
    private ESIndexService          esIndexService;

    @Autowired
    private ESIndicesStatsService esIndicesStatsService;

    private static final Map<String/*cluster@index*/, IndexMetrics /*上一次采集到的索引数据*/> index2LastTimeIndexMetricsMap = Maps.newConcurrentMap();

    private static final FutureUtil FUTURE_UTIL = FutureUtil.init("IndexDashBoardCollector", 10, 10, 100);

    @Override
    public void collectSingleCluster(String cluster, long currentTime) {
        List<CatIndexResult> catIndexResults = esIndexService.syncCatIndex(cluster, 2);
        if (CollectionUtils.isEmpty(catIndexResults)) { return;}


        List<DashBoardStats> dashBoardStatsList = Lists.newArrayList();
        AtomicReference<List<Segment>>                                segmentsListRef         = new AtomicReference<>(Lists.newArrayList());
        AtomicReference<Tuple<List<ShardMetrics>, List<ShardMetrics>>> bigAndSmallListTupleRef = new AtomicReference<>();
        AtomicReference<Map<String, IndexConfig>>                      index2IndexConfigMapRef = new AtomicReference<>(Maps.newHashMap());
        AtomicReference<Map<String, Double>>                      index2IndexingIndexIncrementMapRef = new AtomicReference<>(Maps.newHashMap());
        AtomicReference<Map<String, Double>>                      index2SearchQueryIncrementMapRef = new AtomicReference<>(Maps.newHashMap());
        // 获取集群mapping信息、读、写文档突增等信息
        List<String> indexList = catIndexResults.stream().map(CatIndexResult::getIndex).distinct().collect(Collectors.toList());

        FUTURE_UTIL.runnableTask(() -> segmentsListRef.set(esShardService.syncGetSegments(cluster)))
                .runnableTask(() -> bigAndSmallListTupleRef.set(esShardService.syncGetBigAndSmallShards(cluster)))
                .runnableTask(() -> index2IndexConfigMapRef.set(batchGetIndexConfigMap(cluster, indexList)))
                .runnableTask(() -> index2IndexingIndexIncrementMapRef.set(batchGetIndexingIndexIncrementMap(cluster, indexList)))
                .runnableTask(() -> index2SearchQueryIncrementMapRef.set(batchGetSearchQueryIncrementMap(cluster, indexList)))
                .waitExecute();

        // 获取集群大shard、小shard
        List<ShardMetrics> bigShardListList   = null != bigAndSmallListTupleRef.get() ? bigAndSmallListTupleRef.get().getV1() : Lists.newArrayList();
        List<ShardMetrics> smallShardListList = null != bigAndSmallListTupleRef.get() ? bigAndSmallListTupleRef.get().getV2() : Lists.newArrayList();
        Map<String, List<ShardMetrics>> index2BigShardListMap = ConvertUtil.list2MapOfList(bigShardListList,
                ShardMetrics::getIndex, c -> c);
        Map<String, List<ShardMetrics>> index2SmallShardListMap = ConvertUtil.list2MapOfList(smallShardListList,
                ShardMetrics::getIndex, c -> c);

        // 获取集群segments
        Map<String, List<Segment>> index2SegmentsListMap = ConvertUtil.list2MapOfList(segmentsListRef.get(),
                Segment::getIndex, c -> c);

        for (CatIndexResult index : catIndexResults) {
            DashBoardStats dashBoardStats = buildInitDashBoardStats(currentTime);
            String uniqueIndexKey = CommonUtils.getUniqueKey(cluster, index.getIndex());
            IndexMetrics indexMetrics = index2LastTimeIndexMetricsMap.getOrDefault(uniqueIndexKey, new IndexMetrics());
            indexMetrics.setTimestamp(currentTime);
            indexMetrics.setIndex(index.getIndex());
            indexMetrics.setCluster(cluster);

            // 1. 是否RED索引
            indexMetrics.setRed(IndexStatusEnum.RED.getStatus().equals(index.getHealth()));
            // 2. 是否单副本索引
            indexMetrics.setSingReplicate(NO_REPLICA.equals(index.getRep()));
            // 3. 是否未分配shard索引
            indexMetrics.setUnassignedShard(IndexStatusEnum.GREEN.getStatus().equals(index.getHealth()));
            // 4. 是否为大shard索引(大于50G)
            indexMetrics.setBigShard(index2BigShardListMap.containsKey(index.getIndex()));
            // 5. 是否为小shard索引(小于1G)
            indexMetrics.setSmallShard(index2SmallShardListMap.containsKey(index.getIndex()));
            // 6. 索引Mapping字段个数
            int mappingNum = 0;
            IndexConfig indexConfig = index2IndexConfigMapRef.get().get(index.getIndex());
            if (null != indexConfig && null != indexConfig.getMappings()) {
                mappingNum = MappingConfigUtil.countMappingFieldNum(indexConfig.getMappings());
            }
            indexMetrics.setMappingNum((long) mappingNum);
            // 7. 索引Segements个数
            List<Segment> indexSegmentList = index2SegmentsListMap.getOrDefault(index.getIndex(), Lists.newArrayList());
            indexMetrics.setSegmentNum((long) indexSegmentList.size());
            // 8. 索引Segements内存大小（MB）
            indexMetrics.setSegmentMemSize(indexSegmentList.stream().mapToDouble(Segment::getMemoSize).sum() * BYTE_TO_MB);
            // 9. 写入文档数突增个数 （上个时间间隔的两倍）
            double indexingIndexIncrementValue = index2IndexingIndexIncrementMapRef.get().getOrDefault(index.getIndex(),0d);
            indexMetrics.setDocUprushNum((long) indexingIndexIncrementValue);
            // 10. 查询请求数突增个数（上个时间间隔的两倍）
            double SearchQueryIncrementValue = index2SearchQueryIncrementMapRef.get().getOrDefault(index.getIndex(),0d);
            indexMetrics.setReqUprushNum((long) SearchQueryIncrementValue);

            dashBoardStats.setIndex(indexMetrics);
            dashBoardStatsList.add(dashBoardStats);

            index2LastTimeIndexMetricsMap.put(uniqueIndexKey, indexMetrics);
        }

        if (CollectionUtils.isEmpty(dashBoardStatsList)) { return;}

        monitorMetricsSender.sendDashboardStats(dashBoardStatsList);
    }

    /**
     * 分批采集 + 计算索引列表SearchQuery突增量
     * @param cluster        集群名称
     * @param indexList      索引名称列表
     * @return               Map<String, Double>
     */
    private Map<String, Double> batchGetSearchQueryIncrementMap(String cluster, List<String> indexList) {
        Map<String, Double> index2CurrentSearchQueryMap = Maps.newHashMap();
        List<List<String>> indexListList = Lists.partition(indexList, 20);
        for (List<String> subIndexList : indexListList) {
            index2CurrentSearchQueryMap.putAll(esIndicesStatsService.getIndex2CurrentSearchQueryMap(cluster, subIndexList));
        }

        // 计算索引列表SearchQuery突增量
        Map<String, Double> index2SearchQueryIncrementMap = Maps.newHashMap();
        for (String index : indexList) {
            String       uniqueIndexKey = CommonUtils.getUniqueKey(cluster, index);
            IndexMetrics indexMetrics   = index2LastTimeIndexMetricsMap.get(uniqueIndexKey);
            if (null == indexMetrics) { index2SearchQueryIncrementMap.put(index, 0d); continue;}

            Double currentTimeSearchQuery = index2CurrentSearchQueryMap.get(index);
            Double lastTimeSearchQuery    = indexMetrics.getReqUprushNum().doubleValue();
            // 计算突增值
            Double incrementValue         = MetricsUtils.computerUprushNum(currentTimeSearchQuery, lastTimeSearchQuery);
            index2SearchQueryIncrementMap.put(index, incrementValue);
        }

        return index2SearchQueryIncrementMap;
    }

    /**
     * 分批采集 + 计算索引列表IndexingIndex突增量
     * @param cluster        集群名称
     * @param indexList      索引名称列表
     * @return               Map<String, Double>
     */
    private Map<String, Double> batchGetIndexingIndexIncrementMap(String cluster, List<String> indexList) {
        Map<String, Double> index2CurrentIndexingIndexMap = Maps.newHashMap();
        List<List<String>> indexListList = Lists.partition(indexList, 20);
        for (List<String> subIndexList : indexListList) {
            index2CurrentIndexingIndexMap.putAll(esIndicesStatsService.getIndex2CurrentIndexingIndexMap(cluster, subIndexList));
        }

        // 计算索引列表IndexingIndex突增量
        Map<String, Double> index2IndexingIndexIncrementMap = Maps.newHashMap();
        for (String index : indexList) {
            String       uniqueIndexKey = CommonUtils.getUniqueKey(cluster, index);
            IndexMetrics indexMetrics   = index2LastTimeIndexMetricsMap.get(uniqueIndexKey);
            if (null == indexMetrics) { index2IndexingIndexIncrementMap.put(index, 0d); continue;}

            Double currentTimeSearchQuery = index2CurrentIndexingIndexMap.get(index);
            Double lastTimeSearchQuery    = indexMetrics.getDocUprushNum().doubleValue();
            // 计算突增值
            Double incrementValue         = MetricsUtils.computerUprushNum(currentTimeSearchQuery, lastTimeSearchQuery);
            index2IndexingIndexIncrementMap.put(index, incrementValue);
        }

        return index2IndexingIndexIncrementMap;
    }

    /**
     * 分批采集
     * @param cluster        集群名称
     * @param indexList      索引名称列表
     * @return               Map<String, IndexConfig>
     */
    private Map<String, IndexConfig> batchGetIndexConfigMap(String cluster, List<String> indexList) {
        Map<String, IndexConfig> index2IndexConfigMap = Maps.newHashMap();
        List<List<String>> indexListList = Lists.partition(indexList, 20);
        for (List<String> subIndexList : indexListList) {
            index2IndexConfigMap.putAll(esIndexService.syncBatchGetIndexConfig(cluster, subIndexList));
        }
        return index2IndexConfigMap;
    }

    @Override
    public void collectAllCluster(List<String> clusterList, long currentTime) {

    }

    @Override
    public String getName() {
        return "IndexDashBoardCollector";
    }
}
