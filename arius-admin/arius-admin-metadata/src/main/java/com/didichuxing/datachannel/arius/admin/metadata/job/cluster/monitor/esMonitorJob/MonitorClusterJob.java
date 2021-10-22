package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.common.OdinData;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.MulityTypeTemplatesInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.*;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.action.ESIndicesSimpleStatsAction;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.action.ESIndicesSimpleStatsResponse;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.action.SimpleCommonStat;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.action.SimpleIndexNodes;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.metrics.CollectMetrics;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.metrics.DCDRMetrics;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.metrics.ESNodeToIndexComputer;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.metrics.MetricsRegister;
import com.didichuxing.datachannel.arius.admin.metadata.utils.MonitorUtil;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpHostUtil;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.dcdr.DCDRIndexStats;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.index.stats.IndicesStatsLevel;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.dcdr.ESGetDCDRStatsResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.model.node.NodeAttributes;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 每个集群的采集任务
 */
public class MonitorClusterJob {

    protected static final ILog LOGGER = LogFactory.getLog(MonitorClusterJob.class);

    private static final String     HOST_NAME               = HttpHostUtil.HOST_NAME;
    private static final long       CLIENT_TO_WITH_MILLS    = 50 * 1000;
    private static final int        ODIN_STEP               = 60;

    // 并行获取集群节点数据时每个批次获取的节点数
    private static final int NODE_STATS_BATCH_SIZE = 10;
    // 并行获取索引数据时每个批次获取的索引数
    private static final int INDEX_STATS_BATCH_SIZE = 100;

    private static final String[]   DATA_FORMATS = new String[] {"_YYYYMM", "YYYYMM", "YYYYMMdd", "_YYYYMMdd", "YYYY-MM-dd", "_YYYY-MM-dd",
                                                                 "MMdd", "YYMM", "_YYMM", "YY-MM", "_YYYY-MM", "YYYY",  "_YYYY.MM.dd", "YYYY.MM.dd"};

    private Pattern pattern   = Pattern.compile("(.*)(_v[1-9]\\d*)(.*)");

    private Cache<String/*cluster@templateName*/, IndexTemplatePhyWithLogic> indexTemplateCache = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES).maximumSize(10000).build();

    private Cache</*ip*/String, Tuple</*host*/String, /*odinNs*/String>> odinIpNsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.DAYS).maximumSize(500).build();

    //采集指标
    private CopyOnWriteArrayList<CollectMetrics> indexWorkOrders;
    private CopyOnWriteArrayList<CollectMetrics> indexToNodeWorkOrders;
    private CopyOnWriteArrayList<CollectMetrics> nodeWorkOrders;
    private CopyOnWriteArrayList<CollectMetrics> nodeToIndexWorkOrders;
    private CopyOnWriteArrayList<CollectMetrics> ingestWorkOrders;
    private CopyOnWriteArrayList<CollectMetrics> dcdrWorkOrders;

    //缓存索引和节点的对应关系
    private List<ESIndexToNodeTempBean> indexToNodeTemps      = new CopyOnWriteArrayList<>();

    //缓存nodeid 与 发送给es的指标数据映射关系
    private Map<String, ESNodeStats> nodeIdEsNodeStatsMap  = new ConcurrentHashMap<>();

    private List<IndexTemplatePhyWithLogic>      indexTemplates        = new CopyOnWriteArrayList<>();

    private ESClient                    esClient;

    private MonitorMetricsSender        monitorMetricsSender;

    private MetricsRegister metricsRegister;

    private Set<String>                 clusterNodeIps = Collections.synchronizedSet(new HashSet<>());

    private ESClusterPhy                thirdpartClusterVO;
    // 多type索引 启用查询映射的信息
    private MulityTypeTemplatesInfo     mulityTypeTemplatesInfo;

    private String                      clusterName;

    private final static FutureUtil     futureUtil = FutureUtil.init("MonitorClusterJob", 3, 3, 10);

    public MonitorClusterJob(ESClient esClient,
                             ESClusterPhy thirdpartClusterVO,
                             List<IndexTemplatePhyWithLogic> indexTemplates,
                             MetricsRegister metricsRegister,
                             MonitorMetricsSender monitorMetricsSender,
                             CopyOnWriteArrayList<CollectMetrics> indexWorkOrders,
                             CopyOnWriteArrayList<CollectMetrics> nodeWorkOrders,
                             CopyOnWriteArrayList<CollectMetrics> indexToNodeWorkOrders,
                             CopyOnWriteArrayList<CollectMetrics> nodeToIndexWorkOrders,
                             CopyOnWriteArrayList<CollectMetrics> ingestWorkOrders,
                             CopyOnWriteArrayList<CollectMetrics> dcdrWorkOrders,
                             MulityTypeTemplatesInfo mulityTypeTemplatesInfo
                             ) {
        this.esClient                 = esClient;
        this.thirdpartClusterVO       = thirdpartClusterVO;
        this.metricsRegister          = metricsRegister;
        this.monitorMetricsSender     = monitorMetricsSender;
        this.indexWorkOrders          = indexWorkOrders;
        this.nodeWorkOrders           = nodeWorkOrders;
        this.indexToNodeWorkOrders    = indexToNodeWorkOrders;
        this.nodeToIndexWorkOrders    = nodeToIndexWorkOrders;
        this.ingestWorkOrders         = ingestWorkOrders;
        this.dcdrWorkOrders           = dcdrWorkOrders;
        this.mulityTypeTemplatesInfo  = mulityTypeTemplatesInfo;
        this.clusterName              = esClient.getClusterName();

        this.indexTemplates.addAll(indexTemplates);
    }

    public void collectData() {
        futureUtil.runnableTask(() -> collectNodeData(esClient, metricsRegister))
                .runnableTask(() -> collectIndexData(esClient, metricsRegister))
                .runnableTask(() -> collectDCDRData(esClient, metricsRegister))
                .waitExecute();

        // 索引的集群节点分布数据
        // 注意：collectIndexToNodeData 依赖 collectNodeData、collectIndexData、collectDCDRData
        // 因此不能一起并行计算
        collectIndexToNodeData();
    }

    /**************************************** private methods ****************************************/
    /**
     * 采集节点信息
     * @param esClient
     * @param metricsRegister
     */
    private void collectNodeData(ESClient esClient, MetricsRegister metricsRegister){
        StopWatch stopWatch = new StopWatch();

        long timestamp = System.currentTimeMillis();

        try {
            stopWatch.start("nodestats_indices");
            Map<String, ClusterNodeStats> clusterNodeStatsMap = getNodeStatsByOnce(esClient);
            stopWatch.stop().start("compute");

            String cluster = esClient.getClusterName();

            List<ESNodeStats> esNodeStatsList = new CopyOnWriteArrayList<>();

            clusterNodeStatsMap.entrySet().parallelStream().forEach( entry -> {
                String nodeId = entry.getKey();
                try {
                    ClusterNodeStats clusterNodeStats = entry.getValue();
                    NodeAttributes attributes       = clusterNodeStats.getAttributes();

                    ESDataTempBean base = new ESDataTempBean();
                    base.setDimension(ESDataTempBean.NODE);
                    base.setCluster(cluster);
                    base.setTimestamp(timestamp);
                    base.setNode(clusterNodeStats.getName());
                    base.setIp(HttpHostUtil.getIpFromTransportAddress(clusterNodeStats.getTransportAddress()));
                    base.setPort(HttpHostUtil.getPortFromTransportAddress(clusterNodeStats.getTransportAddress()));
                    base.setRack(null == attributes ? "" : attributes.getRack());

                    clusterNodeIps.add(base.getIp());

                    Map map = JSON.parseObject(JSON.toJSONString(clusterNodeStats), Map.class);
                    // 根据nodeWorkOrders的指定获取指标，生成对应的ESDataTempBean，返回esDataTempBeans的size等于nodeWorkOrders的size（一一对应）
                    List<ESDataTempBean> esDataTempBeans = aggrAndComputeData(map, nodeWorkOrders, base, metricsRegister);

                    // 节点数据
                    ESNodeStats esNodeStats = buildESNodeStats(base, esDataTempBeans);

                    esNodeStatsList.add(esNodeStats);
                    // 节点id到节点数据的map
                    nodeIdEsNodeStatsMap.put(nodeId, esNodeStats);
                    // 获取节点上的索引维度的数据
                    achieveAndSendNodeToIndexInfo(base, map, metricsRegister);

                    // 获取ingest指标
                    achieveAndSendIngestInfo(base, map, metricsRegister);

                    sendOdin(esDataTempBeans);
                } catch (Exception e) {
                    LOGGER.error("class=MonitorClusterJob||method=collectNodeData||nodeId={}||clusterName={}||msg=exception",
                            nodeId, clusterName, e);
                }
            } );

            monitorMetricsSender.sendNodeInfo(esNodeStatsList);
        } catch (Exception e) {
            LOGGER.error("class=MonitorClusterJob||method=collectNodeData||clusterName={}||msg=exception", clusterName, e);
        }

        //必须要放到cache外层，在获取es数据超时时也要进行上报
        collectMonitorData2Odin("node", (double) System.currentTimeMillis() - timestamp, esClient.getClusterName());

        if (stopWatch.isRunning()) {
            stopWatch.stop();
        }
    }

    /**
     * 一次获取集群的所有节点stats
     * @param esClient esClient
     * @return 集群节点stats，key-节点ID，value-节点stats
     */
    private Map<String, ClusterNodeStats> getNodeStatsByOnce(ESClient esClient) {
        ESClusterNodesStatsResponse response = esClient.admin().cluster().prepareNodeStats()
            .level( IndicesStatsLevel.INDICES.getStr())
            .execute()
            .actionGet(CLIENT_TO_WITH_MILLS);
        return response.getNodes();
    }

    /**
     * 采集索引信息
     * @param esClient
     * @param metricsRegister
     */
    private void collectIndexData(ESClient esClient, MetricsRegister metricsRegister) {
        StopWatch stopWatch = new StopWatch();

        long timestamp = System.currentTimeMillis();

        try {
            stopWatch.start("stats_shards");
            // 用于减少反序列化时
            Map<String, SimpleIndexNodes> indexSimpleStatsMap = getIndexSimpleStatsByOnce(esClient);
            stopWatch.stop().start("compute");

            String cluster = esClient.getClusterName();

            // 存储多type改造 查询原索引的统计信息，需要将查询映射后的单type索引的统计信息进行汇聚
            Map<String/*source templateName*/, List<ESIndexStats>/*dest index stats*/> destTemplateIndexStatsMap = Maps.newHashMap();

            // 存储多type改造 查询原索引的统计信息
            Map<String/*source templateName*/, ESIndexStats/*source index stats*/> sourceTemplateSampleIndexStatsMap = Maps.newHashMap();

            List<ESIndexStats> esIndexStatsList = new CopyOnWriteArrayList();

            indexSimpleStatsMap.entrySet().parallelStream().forEach( entry -> {
                String index = entry.getKey();
                try {
                    if(indexSkip(index)){return;}

                    IndexTemplatePhyWithLogic indexTemplate = getTemplateNameForCache(cluster, index);
                    if (null == indexTemplate) {
                        LOGGER.error("class=MonitorJobHandler||method=collectIndexData||index={}||msg=not find indexTemplate", index);
                        return;
                    }

                    SimpleIndexNodes indexStats = entry.getValue();

                    ESDataTempBean base = new ESDataTempBean();
                    base.setDimension(ESDataTempBean.INDEX);
                    base.setCluster(cluster);
                    base.setTimestamp(timestamp);
                    base.setIndex(index);
                    base.setTemplate(indexTemplate.getName());
                    base.setTemplateId(indexTemplate.getId());
                    base.setLogicTemplateId(indexTemplate.getLogicId());
                    base.setShardNu(null == indexStats.getShards() ? 0 : indexStats.getShards().size());

                    Map map = JSON.parseObject(JSON.toJSONString(indexStats.getPrimaries()), Map.class);
                    List<ESDataTempBean> esDataTempBeans = aggrAndComputeData(map, indexWorkOrders, base, metricsRegister);

                    esDataTempBeans.addAll(genIndexTotalCommonStatsMetric(indexStats.getTotal().getStore().getSizeInBytes(),
                        indexStats.getTotal().getDocs().getCount(), base));

                    ESIndexStats esIndexStats = buildESIndexStats(base, esDataTempBeans);

                    // 判断是否为多type索引映射后的单type索引
                    if (Objects.nonNull(mulityTypeTemplatesInfo)) {

                        // 该索引统计信息是映射后的单type信息，需要临时保存
                        if (MapUtils.isNotEmpty(mulityTypeTemplatesInfo.getDest2SourceTemplateMap()) &&
                            mulityTypeTemplatesInfo.getDest2SourceTemplateMap().containsKey(indexTemplate.getName())) {
                            String sourceTemplateName = mulityTypeTemplatesInfo.getDest2SourceTemplateMap().get(indexTemplate.getName());

                            ESIndexStats esIndexStatsCopy = new ESIndexStats();
                            BeanUtils.copyProperties(esIndexStats, esIndexStatsCopy);
                            Map<String, String> metricsCopy = Maps.newHashMap();
                            metricsCopy.putAll(esIndexStats.getMetrics());
                            esIndexStatsCopy.setMetrics(metricsCopy);

                            destTemplateIndexStatsMap.computeIfAbsent(sourceTemplateName, key -> Lists.newArrayList()).add(esIndexStatsCopy);
                        }

                        // 该索引统计信息是源索引信息
                        if (MapUtils.isNotEmpty(mulityTypeTemplatesInfo.getSource2DestTemplateMap()) &&
                            mulityTypeTemplatesInfo.getSource2DestTemplateMap().containsKey(indexTemplate.getName())) {
                            // 丢弃原索引指标值，不加入待发送索引指标集合中
                            sourceTemplateSampleIndexStatsMap.put(indexTemplate.getName(), esIndexStats);
                            return;
                        }
                    }

                    esIndexStatsList.add(esIndexStats);

                    // 索引的集群节点分布信息
                    achieveIndexToNodeInfo(base, indexStats);

                    // 指标发送给odin
                    sendOdin(esDataTempBeans);
                } catch (Exception e) {
                    LOGGER.error("class=MonitorClusterJob||method=collectIndexData||index={}||clusterName={}||msg=exception",
                        index, clusterName, e);
                }
            } );

            addSourceTemplateIndexStats(destTemplateIndexStatsMap, sourceTemplateSampleIndexStatsMap, esIndexStatsList);

            // 指标持久化（写到ES）
            monitorMetricsSender.sendIndexInfo(esIndexStatsList);
        } catch (Exception e) {
            LOGGER.error("class=MonitorClusterJob||method=collectIndexData||clusterName={}||msg=exception", clusterName, e);
        }

        //必须要放到cache外层，在获取es数据超时时也要进行上报
        collectMonitorData2Odin("index", (double) System.currentTimeMillis() - timestamp, esClient.getClusterName());

        if (stopWatch.isRunning()) {
            stopWatch.stop();
        }
    }

    // 一次获取所有索引stat-减少反序列化字段
    private Map<String, SimpleIndexNodes> getIndexSimpleStatsByOnce(ESClient esClient) {
        ESIndicesSimpleStatsResponse response = esClient.admin().indices().prepareExecute(new ESIndicesSimpleStatsAction())
            .setLevel( IndicesStatsLevel.SHARDS).execute().actionGet(CLIENT_TO_WITH_MILLS);
        return response.getIndicesMap();
    }

    /**
     * 计算索引到节点的信息
     */
    private void collectIndexToNodeData() {
        StopWatch stopWatch = new StopWatch();

        try {
            stopWatch.start("index_node");
            List<ESIndexToNodeStats> esIndexToNodeStatsList = new ArrayList<>();
            for (ESIndexToNodeTempBean temp : indexToNodeTemps) {

                for (String nodeId : temp.getNodes()) {
                    ESNodeStats esNodeStats = nodeIdEsNodeStatsMap.get(nodeId);
                    esIndexToNodeStatsList.add(build(temp, esNodeStats, indexToNodeWorkOrders));
                }
            }

            monitorMetricsSender.sendIndexToNodeStats(esIndexToNodeStatsList);
        } catch (Exception e) {
            LOGGER.error("class=MonitorClusterJob||method=collectIndexToNodeData||clusterName={}||msg=exception", clusterName, e);
        }
        if (stopWatch.isRunning()) {
            stopWatch.stop();
        }
    }

    /**
     * 采集DCDR监控信息
     * @param esClient
     * @param metricsRegister
     */
    private void collectDCDRData(ESClient esClient, MetricsRegister metricsRegister) {
        StopWatch stopWatch = new StopWatch();

        try {
            stopWatch.start("dcdr");

            ESGetDCDRStatsResponse response = esClient.admin().indices().prepareGetDCDRStats().execute().actionGet(CLIENT_TO_WITH_MILLS);
            long timestamp = System.currentTimeMillis();
            String cluster = esClient.getClusterName();
            List<ESIndexDCDRStats> esIndexDCDRStatsList = new ArrayList<>();
            response.getIndicesStats().forEach((index, indexStats) -> {
                try {
                    if (indexSkip(index)) {
                        return ;
                    }

                    IndexTemplatePhyWithLogic indexTemplate = getTemplateNameForCache(cluster, index);
                    if (null == indexTemplate) {
                        return;
                    }

                    ESDataTempBean base = new ESDataTempBean();
                    base.setDimension(ESDataTempBean.INDEX);
                    base.setCluster(cluster);
                    base.setTimestamp(timestamp);
                    base.setIndex(index);
                    base.setTemplate(indexTemplate.getName());
                    base.setTemplateId(indexTemplate.getId());
                    base.setLogicTemplateId(indexTemplate.getLogicId());
                    base.setShardNu(indexStats.getDcdrStats().size());

                    Map<String, DCDRMetrics> indexStatsByCluster = aggrAndComputeDCDRIndexData(indexStats);
                    indexStatsByCluster.forEach((replicaCluster, dcdrMetrics) -> {
                        Map map = (Map) JSON.toJSON(dcdrMetrics);
                        List<ESDataTempBean> esDataTempBeans = aggrAndComputeData(map, dcdrWorkOrders, base, metricsRegister);
                        ESIndexDCDRStats esIndexDCDRStats = buildESIndexDCDRStats(base, replicaCluster, esDataTempBeans);
                        esIndexDCDRStatsList.add(esIndexDCDRStats);
                        sendOdin(esDataTempBeans);
                    });
                } catch (Exception e) {
                    LOGGER.error("class=MonitorClusterJob||method=collectDcdrData||index={}||clusterName={}||msg=exception",
                            index, clusterName, e);
                }
            });

            monitorMetricsSender.sendDCDRStats(esIndexDCDRStatsList);
        } catch (Exception e) {
            LOGGER.error("class=MonitorClusterJob||method=collectDcdrData||clusterName={}||msg=dcdr_exception", clusterName, e);
        }

        if (stopWatch.isRunning()) {
            stopWatch.stop();
        }
    }

    private Map<String, DCDRMetrics> aggrAndComputeDCDRIndexData(DCDRIndexStats indexStats) {
        Map<String, DCDRMetrics> indexStatsByCluster = new HashMap<>();
        indexStats.getDcdrStats().forEach((shardId, statsList) -> {
            statsList.forEach(dcdrStats -> {
                DCDRMetrics indexDCDRMetrics;
                if (indexStatsByCluster.containsKey(dcdrStats.getReplicaCluster())) {
                    indexDCDRMetrics = indexStatsByCluster.get(dcdrStats.getReplicaCluster());
                } else {
                    indexDCDRMetrics = new DCDRMetrics();
                    indexStatsByCluster.put(dcdrStats.getReplicaCluster(), indexDCDRMetrics);
                }

                if (dcdrStats.getReplicaGlobalCheckpoint() >= 0) {
                    indexDCDRMetrics.setGlobalCheckpointDelay(indexDCDRMetrics.getGlobalCheckpointDelay()
                            + dcdrStats.getPrimaryGlobalCheckpoint() - dcdrStats.getReplicaGlobalCheckpoint());
                }
                if (dcdrStats.getReplicaMaxSeqNo() >= 0) {
                    indexDCDRMetrics.setMaxSeqNoDelay(indexDCDRMetrics.getMaxSeqNoDelay()
                            + dcdrStats.getPrimaryMaxSeqNo() - dcdrStats.getReplicaMaxSeqNo());
                }
                if (dcdrStats.getAvailableSendBulkNumber() < indexDCDRMetrics.getMinAvailableSendBulkNumber()) {
                    indexDCDRMetrics.setMinAvailableSendBulkNumber(dcdrStats.getAvailableSendBulkNumber());
                }
                indexDCDRMetrics.setTotalSendTimeMillis(indexDCDRMetrics.getTotalSendTimeMillis()
                        + dcdrStats.getTotalSendTimeMillis());
                indexDCDRMetrics.setTotalSendRequests(indexDCDRMetrics.getTotalSendRequests()
                        + dcdrStats.getSuccessfulSendRequests() + dcdrStats.getFailedSendRequests());
                indexDCDRMetrics.setFailedSendRequests(indexDCDRMetrics.getFailedRecoverCount()
                        + dcdrStats.getFailedSendRequests());
                indexDCDRMetrics.setOperationsSend(indexDCDRMetrics.getOperationsSend()
                        + dcdrStats.getOperationsSends());
                indexDCDRMetrics.setBytesSend(indexDCDRMetrics.getBytesSend()
                        + dcdrStats.getBytesSend());
                if (dcdrStats.getTimeSinceLastSendMillis() < indexDCDRMetrics.getMinTimeSinceLastSendMillis()) {
                    indexDCDRMetrics.setMinTimeSinceLastSendMillis(dcdrStats.getTimeSinceLastSendMillis());
                }
                if (dcdrStats.getTimeSinceLastSendMillis() > indexDCDRMetrics.getMaxTimeSinceLastSendMillis()
                        && dcdrStats.getReplicaGlobalCheckpoint() >= 0
                        && dcdrStats.getPrimaryGlobalCheckpoint() != dcdrStats.getReplicaGlobalCheckpoint()) {
                    indexDCDRMetrics.setMaxTimeSinceLastSendMillis(dcdrStats.getTimeSinceLastSendMillis());
                }
                if (dcdrStats.getTimeSinceUpdateReplicaCheckPoint() > indexDCDRMetrics.getMaxTimeSinceLastSendMillis()
                        && dcdrStats.getReplicaGlobalCheckpoint() >= 0
                        && dcdrStats.getPrimaryGlobalCheckpoint() != dcdrStats.getReplicaGlobalCheckpoint()) {
                    indexDCDRMetrics.setMaxTimeSinceUpdateReplicaCheckPoint(dcdrStats.getTimeSinceUpdateReplicaCheckPoint());
                }

                indexDCDRMetrics.setSuccessRecoverCount(indexDCDRMetrics.getSuccessRecoverCount()
                        + dcdrStats.getSuccessRecoverCount());
                indexDCDRMetrics.setFailedRecoverCount(indexDCDRMetrics.getFailedRecoverCount()
                        + dcdrStats.getFailedRecoverCount());

                int inSyncSize;
                if (dcdrStats.getInSyncOffset().size() == 1 && dcdrStats.getInSyncOffset().get(0) != null) {
                    inSyncSize = dcdrStats.getInSyncOffset().get(0).size();
                } else {
                    inSyncSize = dcdrStats.getInSyncOffset().size();
                }
                indexDCDRMetrics.setInSyncTranslogOffsetSize(indexDCDRMetrics.getInSyncTranslogOffsetSize()
                        + inSyncSize);
                indexDCDRMetrics.setRecoverTotalTimeMillis(indexDCDRMetrics.getRecoverTotalTimeMillis()
                        + dcdrStats.getRecoverTotalTimeMillis());
            });
        });

        return indexStatsByCluster;
    }

    private ESIndexDCDRStats buildESIndexDCDRStats(ESDataTempBean bean, String replicaCluster, List<ESDataTempBean> metricsList) {
        ESIndexDCDRStats esIndexDCDRStats = new ESIndexDCDRStats();
        esIndexDCDRStats.setTimestamp(bean.getTimestamp());
        esIndexDCDRStats.setCluster(bean.getCluster());
        esIndexDCDRStats.setReplicaCluster(replicaCluster);
        esIndexDCDRStats.setTemplate(bean.getTemplate());
        esIndexDCDRStats.setTemplateId(bean.getTemplateId());
        esIndexDCDRStats.setLogicTemplateId(bean.getLogicTemplateId());
        esIndexDCDRStats.setIndex(bean.getIndex());
        esIndexDCDRStats.setShardNu(bean.getShardNu());
        esIndexDCDRStats.setMetrics(Maps.newHashMap());

        for (ESDataTempBean esDataTempBean : metricsList) {
            if (StringUtils.isEmpty(esDataTempBean.getComputeValue()) || "null".equals(esDataTempBean.getComputeValue())) {
                continue;
            }

            esIndexDCDRStats.putMetrics(esDataTempBean.getValueName().substring("es.indices.".length()).replaceAll("\\.", "-"),
                    esDataTempBean.getComputeValue());
        }

        return esIndexDCDRStats;
    }

    /**
     * 向odin发送各个任务的执行时间
     * @param type
     * @param value
     */
    private void collectMonitorData2Odin(String type, Double value, String cluster) {
        OdinData format = new OdinData();
        format.setTimestamp(System.currentTimeMillis() / 1000);
        format.putTag("host",    HOST_NAME);
        format.putTag("cluster", cluster);
        format.putTag("type",    type);
        format.putTag("level",   String.valueOf(thirdpartClusterVO.getLevel()));
        format.setStep(ODIN_STEP);
        format.setName("monitor.metadata.collect.time");
        format.setValue(String.valueOf(value));

        monitorMetricsSender.sendOdinData(Arrays.asList(format));
    }

    private void sendOdin(List<ESDataTempBean> esDataTempBeans){
        List<OdinData> odinDataFormats = new ArrayList<>();
        for(ESDataTempBean esDataTempBean : esDataTempBeans){
            if(esDataTempBean.isSendToOdin()){
                OdinData odinDataFormat = MonitorUtil.fillDataformat(esDataTempBean);
                odinDataFormat.putTag("level",   String.valueOf(thirdpartClusterVO.getLevel()));
                odinDataFormats.add(odinDataFormat);
            }
        }

        monitorMetricsSender.sendOdinData(odinDataFormats);
    }

    private void achieveIndexToNodeInfo(ESDataTempBean base, SimpleIndexNodes indexStats) {
        try {
            ESIndexToNodeTempBean temp = new ESIndexToNodeTempBean();
            temp.setTimestamp(base.getTimestamp());
            temp.setCluster(base.getCluster());
            temp.setTemplate(base.getTemplate());
            temp.setTemplateId(base.getTemplateId());
            temp.setLogicTemplateId(base.getLogicTemplateId());
            temp.setIndex(base.getIndex());

            Map<String, List<SimpleCommonStat>> shards = indexStats.getShards();

            Set<String> nodeIds = Sets.newHashSet();
            for (String key : shards.keySet()) {
                for (SimpleCommonStat stat : shards.get(key)) {
                    nodeIds.add(stat.getRouting().getNode());
                }
            }

            temp.setNodes(nodeIds);
            indexToNodeTemps.add(temp);
        } catch (Exception e) {
            LOGGER.error("class=MonitorClusterJob||method=achieveIndexToNodeInfo||clusterName={}||msg=exception", clusterName, e);
        }
    }

    private void achieveAndSendNodeToIndexInfo(ESDataTempBean node, Map map, MetricsRegister metricsRegister) {
        try {
            Map indicesTotalMap = (Map) map.get("indices");
            Map indicesMap      = (Map) indicesTotalMap.get("indices");

            List<ESNodeToIndexStats> esNodeToIndexStatsList  = new ArrayList<>();
            Map<String, List<ESNodeToIndexTempBean>> dataBeanMap = collectNodeToIndexData(node, indicesMap, metricsRegister);
            for (List<ESNodeToIndexTempBean> beanList : dataBeanMap.values()) {
                esNodeToIndexStatsList.add(buildEsNodeToIndexStats(beanList.get(0), beanList, node.getTimestamp()));
            }

            monitorMetricsSender.sendESNodeToIndexStats(esNodeToIndexStatsList);
        } catch (Exception e) {
            LOGGER.error("class=MonitorClusterJob||method=achieveAndSendNodeToIndexInfo||clusterName={}||msg=exception", clusterName, e);
        }
    }

    private void achieveAndSendIngestInfo(ESDataTempBean node, Map map, MetricsRegister metricsRegister) {
        try {
            Map ingest = (Map) map.get("ingest");
            if (ingest == null) {
                return ;
            }

            Map pipelines = (Map) ingest.get("pipelines");
            if (pipelines == null) {
                return ;
            }

            List<ESIngestStats> esIngestStatsList  = new ArrayList<>();
            Map<String, List<ESNodeToIndexTempBean>> dataBeanMap = collectIngestData(node, pipelines, metricsRegister);
            for (List<ESNodeToIndexTempBean> beanList : dataBeanMap.values()) {
                esIngestStatsList.add(buildESIngestStats(beanList.get(0), beanList, node.getTimestamp()));
            }

            monitorMetricsSender.sendIngestStats(esIngestStatsList);
        } catch (Exception e) {
            LOGGER.error("class=MonitorClusterJob||method=achieveAndSendIngestInfo||clusterName={}||msg=exception", clusterName, e);
        }
    }

    /**
     * 收集ingest指标
     * @param node 节点
     * @param pipelines ingest pipelines指标
     * @param metricsRegister
     * @return
     */
    private Map<String, List<ESNodeToIndexTempBean>> collectIngestData(ESDataTempBean node, Map pipelines, MetricsRegister metricsRegister) {
        Map<String, List<ESNodeToIndexTempBean>> pipelinesStats = Maps.newHashMap();
        for (Object entry : pipelines.entrySet()) {
            Map.Entry ingestEntry = (Map.Entry) entry;
            String template = (String) ingestEntry.getKey();

            if (template.startsWith(".")) {
                continue ;
            }

            IndexTemplatePhyWithLogic indexTemplate = getIndexTemplateByTemplateName(node.getCluster(), template);
            if (null == indexTemplate) {
                continue ;
            }

            Map pipelineStats = (Map) ingestEntry.getValue();

            ESNodeToIndexTempBean base = new ESNodeToIndexTempBean();
            base.setTimestamp(node.getTimestamp());
            base.setCluster(node.getCluster());
            base.setNode(node.getNode());
            base.setPort(node.getPort());
            base.setIndex(template);
            base.setTemplate(indexTemplate.getName());
            base.setTemplateId(indexTemplate.getId());
            base.setLogicTemplateId(indexTemplate.getLogicId());

            List<ESNodeToIndexTempBean> dataBeans = aggrAndComputeNodeToIndexData(pipelineStats, ingestWorkOrders, base, metricsRegister);
            pipelinesStats.put(template, dataBeans);
        }

        return pipelinesStats;
    }

    /**
     * 采集节点上的索引维度的数据
     */
    private Map<String, List<ESNodeToIndexTempBean>> collectNodeToIndexData(ESDataTempBean node, Map indexStatMap, MetricsRegister metricsRegister) {
        Map<String, List<ESNodeToIndexTempBean>> indexStats = Maps.newHashMap();
        for (Object entry : indexStatMap.entrySet()) {
            Map.Entry indexStatEntry = (Map.Entry) entry;
            String indexName = (String) indexStatEntry.getKey();
            if (indexSkip(indexName)) {
                continue;
            }

            IndexTemplatePhyWithLogic indexTemplate = getTemplateNameForCache(node.getCluster(), indexName);
            if(null == indexTemplate){continue;}

            Map indexStat = (Map) indexStatEntry.getValue();

            ESNodeToIndexTempBean base = new ESNodeToIndexTempBean();
            base.setTimestamp(node.getTimestamp());
            base.setCluster(node.getCluster());
            base.setNode(node.getNode());
            base.setPort(node.getPort());
            base.setIndex(indexName);
            base.setTemplate(indexTemplate.getName());
            base.setTemplateId(indexTemplate.getId());
            base.setLogicTemplateId(indexTemplate.getLogicId());

            List<ESNodeToIndexTempBean> dataBeans = aggrAndComputeNodeToIndexData(indexStat, nodeToIndexWorkOrders, base, metricsRegister);
            indexStats.put(indexName, dataBeans);
        }
        return indexStats;
    }

    private List<ESNodeToIndexTempBean> aggrAndComputeNodeToIndexData(Map indexStat,
                                                                      CopyOnWriteArrayList<CollectMetrics> workOrders,
                                                                      ESNodeToIndexTempBean base,
                                                                      MetricsRegister metricsRegister) {
        List<ESNodeToIndexTempBean> result = Lists.newArrayList();
        for (CollectMetrics workOrder : workOrders) {
            try {
                String valueName  = workOrder.getValueName();
                String valueRoute = workOrder.getValueRoute();
                Double value      = MonitorUtil.obj2Double( MonitorUtil.getValueByRoute(indexStat, valueRoute));

                ESNodeToIndexTempBean dataBean = new ESNodeToIndexTempBean();
                BeanUtils.copyProperties(base, dataBean);
                dataBean.setValueName(valueName);
                dataBean.setValue(value);
                dataBean.setDeriveParam(workOrder.getDeriveParam());
                dataBean.setTimestamp(base.getTimestamp());

                ESNodeToIndexComputer computer = new ESNodeToIndexComputer(workOrder.getComputeType(), metricsRegister);
                String computerValue = computer.compute(dataBean);

                dataBean.setComputeValue(computerValue);

                result.add(dataBean);
            }catch (Exception e){
                LOGGER.error("class=MonitorClusterJob||method=aggrAndComputeNodeToIndexData||clusterName={}||msg=exception", clusterName, e);
            }
        }
        return result;
    }

    /**
     * 将map中的数据交给计算模块去处理
     * workOrders之间不能并发处理，workOrders之间会有相互依赖
     * @param base
     */
    private List<ESDataTempBean> aggrAndComputeData(Map map, CopyOnWriteArrayList<CollectMetrics> workOrders,
                                                    ESDataTempBean base, MetricsRegister metricsRegister) {
        List<ESDataTempBean> result = Lists.newArrayList();
        for (CollectMetrics workOrder : workOrders) {
            try {
                String valueName  = workOrder.getValueName();
                String valueRoute = workOrder.getValueRoute();
                Double value      = MonitorUtil.obj2Double( MonitorUtil.getValueByRoute(map, valueRoute));

                ESDataTempBean esDataTempBean = new ESDataTempBean();
                BeanUtils.copyProperties(base, esDataTempBean);
                esDataTempBean.setValueName(valueName);
                esDataTempBean.setValue(value);
                esDataTempBean.setDeriveParam(workOrder.getDeriveParam());
                esDataTempBean.setSendToOdin(workOrder.isSendToOdin());
                esDataTempBean.setComputeValue(workOrder.getComputeType().getComputer(metricsRegister).compute(esDataTempBean));

                result.add(esDataTempBean);
            }catch (Exception e){
                LOGGER.error("class=MonitorClusterJob||method=aggrAndComputeData||clusterName={}||msg=exception", clusterName, e);
            }
        }
        return result;
    }

    private ESIndexToNodeStats build(ESIndexToNodeTempBean temp, ESNodeStats esNodeStats,
                                     CopyOnWriteArrayList<CollectMetrics> indexToNodeWorkOrders) {
        Set<String> metricsNameSet = Sets.newHashSet();
        for (CollectMetrics workOrder : indexToNodeWorkOrders) {
            metricsNameSet.add(workOrder.getValueName().substring("es.node.".length()).replaceAll("\\.", "-"));
        }

        ESIndexToNodeStats indexToNodeStats = new ESIndexToNodeStats();
        BeanUtils.copyProperties(temp, indexToNodeStats);
        Map<String, String> metrics = Maps.newHashMap();

        if (esNodeStats != null) {
            indexToNodeStats.setNode(esNodeStats.getNode());
            indexToNodeStats.setPort(esNodeStats.getPort());
            indexToNodeStats.setRack(esNodeStats.getRack());

            Map<String, String> metricsAll = esNodeStats.getMetrics();

            for (String key : metricsAll.keySet()) {
                if (metricsNameSet.contains(key)) {
                    metrics.put(key, metricsAll.get(key));
                }
            }
        }
        indexToNodeStats.setMetrics(metrics);

        return indexToNodeStats;
    }

    private ESNodeStats buildESNodeStats(ESDataTempBean bean, List<ESDataTempBean> metricsList) {
        ESNodeStats esNodeStats = new ESNodeStats();
        esNodeStats.setTimestamp(bean.getTimestamp());
        esNodeStats.setCluster(bean.getCluster());
        esNodeStats.setNode(bean.getNode());
        esNodeStats.setIp(bean.getIp());
        esNodeStats.setPort(bean.getPort());
        esNodeStats.setMetrics(Maps.newHashMap());
        esNodeStats.setRack(bean.getRack());

        for (ESDataTempBean esDataTempBean : metricsList) {
            if (StringUtils.isEmpty(esDataTempBean.getComputeValue()) || "null".equals(esDataTempBean.getComputeValue())) {
                continue;
            }

            esNodeStats.putMetrics(esDataTempBean.getValueName().substring("es.node.".length()).replaceAll("\\.", "-"),
                    esDataTempBean.getComputeValue());
        }

        return esNodeStats;
    }

    private ESIndexStats buildESIndexStats(ESDataTempBean bean, List<ESDataTempBean> metricsList) {
        ESIndexStats esIndexStats = new ESIndexStats();
        esIndexStats.setTimestamp(bean.getTimestamp());
        esIndexStats.setCluster(bean.getCluster());
        esIndexStats.setTemplate(bean.getTemplate());
        esIndexStats.setTemplateId(bean.getTemplateId());
        esIndexStats.setLogicTemplateId(bean.getLogicTemplateId());
        esIndexStats.setIndex(bean.getIndex());
        esIndexStats.setShardNu(bean.getShardNu());
        esIndexStats.setMetrics(Maps.newHashMap());

        for (ESDataTempBean esDataTempBean : metricsList) {
            if (StringUtils.isEmpty(esDataTempBean.getComputeValue()) || "null".equals(esDataTempBean.getComputeValue())) {
                continue;
            }

            esIndexStats.putMetrics(esDataTempBean.getValueName().substring("es.indices.".length()).replaceAll("\\.", "-"),
                    esDataTempBean.getComputeValue());
        }

        return esIndexStats;
    }

    private ESNodeToIndexStats buildEsNodeToIndexStats(ESNodeToIndexTempBean base, List<ESNodeToIndexTempBean> metricsList, long timestamp) {
        ESNodeToIndexStats esNodeToIndexStats = new ESNodeToIndexStats();
        esNodeToIndexStats.setCluster(base.getCluster());
        esNodeToIndexStats.setIndex(base.getIndex());
        esNodeToIndexStats.setNode(base.getNode());
        esNodeToIndexStats.setPort(base.getPort());
        esNodeToIndexStats.setTemplate(base.getTemplate());
        esNodeToIndexStats.setTemplateId(base.getTemplateId());
        esNodeToIndexStats.setLogicTemplateId(base.getLogicTemplateId());
        esNodeToIndexStats.setTimestamp(timestamp);
        for (ESNodeToIndexTempBean bean : metricsList) {
            if (StringUtils.isEmpty(bean.getComputeValue())
                    || "null".equals(bean.getComputeValue())) {
                continue;
            }

            esNodeToIndexStats.putMetrics(bean.getValueName().substring("es.node.index.".length()).replaceAll("\\.", "-"),
                    bean.getComputeValue());
        }

        return esNodeToIndexStats;
    }

    /**
     * 构建ingest的指标
     * @param base base指标
     * @param metricsList 指标列表
     * @param timestamp 时间戳
     * @return ESIngestStats对象
     */
    private ESIngestStats buildESIngestStats(ESNodeToIndexTempBean base, List<ESNodeToIndexTempBean> metricsList, long timestamp) {
        ESIngestStats ingestStats = new ESIngestStats();
        ingestStats.setCluster(base.getCluster());
        ingestStats.setNode(base.getNode());
        ingestStats.setPort(base.getPort());
        ingestStats.setTemplate(base.getTemplate());
        ingestStats.setTemplateId(base.getTemplateId());
        ingestStats.setLogicTemplateId(base.getLogicTemplateId());
        ingestStats.setTimestamp(timestamp);
        for (ESNodeToIndexTempBean bean : metricsList) {
            if (StringUtils.isEmpty(bean.getComputeValue())
                    || "null".equals(bean.getComputeValue())) {
                continue;
            }

            ingestStats.putMetrics(bean.getValueName().substring("es.node.".length()).replaceAll("\\.", "-"),
                    bean.getComputeValue());
        }

        return ingestStats;
    }

    /**
     * 根据索引获取模板名称
     * @param cluster
     * @param indexName
     * @return
     */
    private IndexTemplatePhyWithLogic getTemplateNameForCache(String cluster, String indexName) {
        try {
            return indexTemplateCache.get(cluster + "@" + indexName, () -> getTemplateName(cluster, indexName));
        } catch (Exception e) {
            if(!EnvUtil.isOnline()){
                LOGGER.warn("class=MonitorJobHandler||method=getTemplateNameForCache||cluster={}||indexName={}" +
                                "||msg=exception, indexName`s IndexTemplate is null!",
                        cluster, indexName);
            }
        }
        return null;
    }

    /**
     * 根据集群和模板名称获取IndexTemplate对象
     * @param cluster 集群名称
     * @param templateName 模板名称
     * @return IndexTemplate对象
     */
    private IndexTemplatePhyWithLogic getIndexTemplateByTemplateName(String cluster, String templateName) {
        for(IndexTemplatePhyWithLogic indexTemplate : indexTemplates) {
            String clusterName = indexTemplate.getCluster();
            String name = indexTemplate.getName();

            if (clusterName.equals(cluster) && name.equals(templateName)) {
                return indexTemplate;
            }
        }

        return null;
    }

    private IndexTemplatePhyWithLogic getTemplateName(String cluster, String indexName) {
        for(IndexTemplatePhyWithLogic indexTemplate : indexTemplates){
            String clusterName      = indexTemplate.getCluster();
            String expression       = indexTemplate.getExpression();
            String expressionWhoutAsterisk  = "";

            if (expression.endsWith("*")) {
                expressionWhoutAsterisk = expression.substring(0, expression.length() - 1);
            } else {
                expressionWhoutAsterisk = expression;
            }

            if (indexName.startsWith(expressionWhoutAsterisk)) {
                String dataFormat   = indexTemplate.getLogicTemplate().getDateFormat();

                if(!expression.endsWith("*") || StringUtils.isEmpty(dataFormat) || "null".equals(dataFormat)) {
                    if(indexName.equals(expression) && cluster.equals(clusterName)){return indexTemplate;}
                }

                if(isMatch(indexName, expression, dataFormat) && cluster.equals(clusterName)){return indexTemplate;}

                //时间后缀变更
                for (String otherFormat : DATA_FORMATS) {
                    if(otherFormat.equals(dataFormat)){continue;}
                    if(isMatch(indexName, expression, otherFormat) && cluster.equals(clusterName)){return indexTemplate;}
                }
            }
        }

        return null;
    }

    private String genIndexNameClear(String indexName, String expression, String dateFormat) {
        Matcher m = pattern.matcher(indexName);

        if (!m.find() || StringUtils.isNotBlank(m.group(3))) {
            //校验是否是当前模板的
            if (indexName.length() != (expression.length() - 1 + dateFormat.length())) {
                return "";
            }
            return indexName;
        }

        String indexNameClear = m.group(1);
        if (indexNameClear.length() != (expression.length() - 1 + dateFormat.length())) {
            return "";
        }

        return indexNameClear;
    }

    private boolean isMatch(String indexName, String expression, String dataFormat) {
        String indexNameNoVersion = genIndexNameClear(indexName, expression, dataFormat);

        if (StringUtils.isNotBlank(indexNameNoVersion)) {
            String indexNameForTemplate = expression.replace("*", "") + dataFormat;
            if (indexNameNoVersion.length() == indexNameForTemplate.length()) {
                return true;
            }
        }

        return false;
    }

    private List<ESDataTempBean> genIndexTotalCommonStatsMetric(long totalSize, long totalDocCount, ESDataTempBean base) {
        ESDataTempBean totalSizeBean = new ESDataTempBean();
        BeanUtils.copyProperties(base, totalSizeBean);
        totalSizeBean.setValueName("es.indices.store.size_in_bytes.total");
        totalSizeBean.setValue((double)totalSize);
        totalSizeBean.setComputeValue(String.valueOf(totalSizeBean.getValue()));

        ESDataTempBean totalDocsBean = new ESDataTempBean();
        BeanUtils.copyProperties(base, totalDocsBean);
        totalDocsBean.setValueName("es.indices.docs.count.total");
        totalDocsBean.setValue((double)totalDocCount);
        totalDocsBean.setComputeValue(String.valueOf(totalDocsBean.getValue()));

        return Lists.newArrayList(totalSizeBean, totalDocsBean);
    }

    private boolean indexSkip(String indexName){
        if(indexName.startsWith(".monitoring")
                || indexName.startsWith(".marvel")
                || indexName.startsWith(".kibana")){return true;}

        return false;
    }

    /**
     * 转换并加入原索引统计信息
     *
     * @param destTemplateIndexStatsMap
     * @param sourceTemplateSampleIndexStatsMap
     * @param esIndexStatsList
     */
    private void addSourceTemplateIndexStats(Map<String, List<ESIndexStats>> destTemplateIndexStatsMap, Map<String, ESIndexStats> sourceTemplateSampleIndexStatsMap, List<ESIndexStats> esIndexStatsList) {
        try {
            // 存储多type改造 查询原索引的统计信息，需要将查询映射后的单type索引的统计信息进行汇聚
            for (Map.Entry<String/*source templateName*/, List<ESIndexStats>/*dest index stats*/> entry : destTemplateIndexStatsMap.entrySet()) {
                // 目标索引统计信息集合，进行指标汇聚
                Map<String/*source index name*/, ESIndexStats/*dest index merge stats*/> sourceIndexNameIndexStatsMap = Maps.newHashMap();
                // 某个索引个数统计用于求均值
                Map<String/*source index name*/, AtomicInteger> sourceIndexNameCountMap = Maps.newHashMap();

                ESIndexStats destMergeIndexStats = null;
                ESIndexStats sourceIndexStats = null;
                for (ESIndexStats destIndexStats : entry.getValue()) {
                    // 将索引名称替换为原索引名称，包含时间分区
                    String sourceIndexName = destIndexStats.getIndex().replaceFirst(destIndexStats.getTemplate(), entry.getKey());
                    destMergeIndexStats = sourceIndexNameIndexStatsMap.get(sourceIndexName);
                    if (Objects.isNull(destMergeIndexStats)) {
                        sourceIndexStats = sourceTemplateSampleIndexStatsMap.get(entry.getKey());
                        if (Objects.nonNull(sourceIndexStats)) {
                            destIndexStats.setLogicTemplateId(sourceIndexStats.getLogicTemplateId());
                            destIndexStats.setTemplate(sourceIndexStats.getTemplate());
                            destIndexStats.setTemplateId(sourceIndexStats.getTemplateId());
                            destIndexStats.setShardNu(sourceIndexStats.getShardNu());
                        }
                        destIndexStats.setIndex(sourceIndexName);
                        sourceIndexNameIndexStatsMap.put(sourceIndexName, destIndexStats);
                        sourceIndexNameCountMap.put(sourceIndexName, new AtomicInteger(1));
                        continue;
                    }
                    // 指标数据合并
                    destMergeIndexStats = mergeIndexStats(destMergeIndexStats, destIndexStats);
                    sourceIndexNameIndexStatsMap.put(sourceIndexName, destMergeIndexStats);
                    sourceIndexNameCountMap.get(sourceIndexName).incrementAndGet();
                }

                // 将指标数据合并后的统计值加入到待发送索引指标集合中
                // 处理求平均的指标项
                for (Map.Entry<String/*source index name*/, ESIndexStats/*dest index merge stats*/> indexNameEntry : sourceIndexNameIndexStatsMap.entrySet()) {
                    Iterator<Map.Entry<String/*metrics name*/, String/*metrics vale*/>> iterator = indexNameEntry.getValue().getMetrics().entrySet().iterator();
                    Map.Entry<String/*metrics name*/, String/*metrics vale*/> metricsEntry = null;
                    Integer count = sourceIndexNameCountMap.get(indexNameEntry.getKey()).get();

                    while (iterator.hasNext()) {
                        metricsEntry = iterator.next();
                        if (metricsEntry.getKey().contains("avg")) {
                            Double sum = Double.valueOf(metricsEntry.getValue());
                            metricsEntry.setValue(String.valueOf(sum / count));
                        }
                    }
                    esIndexStatsList.add(indexNameEntry.getValue());
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=MonitorClusterJob||method=addSourceTemplateIndexStats||clusterName={}||msg=exception", clusterName, e);
        }
    }

    /**
     * 索引指标数据合并
     *
     * @param mergeIndexStats
     * @param indexStats
     * @return
     */
    private ESIndexStats mergeIndexStats(ESIndexStats mergeIndexStats, ESIndexStats indexStats) {

        String num1 = "", num2 = "";
        String key = "";
        try {
            Iterator<Map.Entry<String/*metrics name*/, String/*metrics vale*/>> iterator = mergeIndexStats.getMetrics().entrySet().iterator();
            Map.Entry<String/*metrics name*/, String/*metrics vale*/> entry = null;
            while (iterator.hasNext()) {
                entry = iterator.next();
                key = entry.getKey();
                num1 = entry.getValue();
                num2 = indexStats.getMetrics().get(key);
                if (StringUtils.isBlank(num1)) {
                    //LOGGER.error("class=MonitorClusterJob||method=mergeIndexStats||errMsg={} num1 is {}", key, num1);
                    num1 = "0";
                }
                if (StringUtils.isBlank(num2)) {
                    //LOGGER.error("class=MonitorClusterJob||method=mergeIndexStats||errMsg={} num2 is {}", key, num2);
                    num2 = "0";
                }
                Double sum = Double.valueOf(num1) + Double.valueOf(num2);
                entry.setValue(String.valueOf(sum));
            }
        } catch (Exception e) {
            LOGGER.error("class=MonitorClusterJob||method=mergeIndexStats||clusterName={}||key={}||num1={}||num2={}||msg=exception",
                    clusterName, key, num1, num2, e);
        }

        return mergeIndexStats;
    }
}
