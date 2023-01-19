package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_FAILED;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.MulityTypeTemplatesInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.MonitorTaskInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.AriusMetaJobClusterDistributePO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.AmsTemplatePhysicalConfVO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpHostUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.metrics.CollectMetrics;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.metrics.MetricsRegister;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor.AriusMetaJobClusterDistributeDAO;
import com.didiglobal.knowframework.elasticsearch.client.ESClient;
import com.didiglobal.knowframework.observability.Observability;
import com.didiglobal.knowframework.observability.conponent.thread.ContextExecutorService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.NoArgsConstructor;

/**
 * 通过查数据库观察每个节点采集es集群名称
 *
 * select * from cluster_monitor_task_v2 where monitor_time > '2020-07-10 15:01:10' and datacentre='cn' order by monitor_host desc  limit 20
 * @author didi
 */
@Component
@NoArgsConstructor
public class MonitorJobHandler extends AbstractMetaDataJob {

    public static final long                                                           TASK_TIMEOUT_TIME                     = 220
                                                                                                                               * 1000L;
    @Autowired
    private AriusMetaJobClusterDistributeDAO                                           ariusMetaJobClusterDistributeDAO;

    @Autowired
    private ClusterPhyService                                                          phyClusterService;

    @Autowired
    private IndexTemplatePhyService                                                    indexTemplatePhyService;

    @Autowired
    private IndexTemplateService                                                       indexTemplateService;

    @Autowired
    private ESOpClient                                                                 esOpClient;

    @Autowired
    private MonitorMetricsSender                                                       monitorMetricsSender;

    @Autowired
    private AriusConfigInfoService                                                     ariusConfigInfoService;

    private MetricsRegister                                                            metricsRegister                       = new MetricsRegister();

    private String                                                                     hostName                              = HttpHostUtil.HOST_NAME;
    private Integer                                                                    clusterTotal                          = -1;

    private String                                                                     dataCentre                            = EnvUtil
        .getDC().getCode();

    private static final Long                                                          MONITOR_TASK_CHECK_MINUTES_WITH_MILLS = EnvUtil
        .isOnline() ? (long) (1000 * 60 * 30) : (long) (1000 * 60 * 5);

    /**
     * 采集指标
     */
    private List<CollectMetrics>                                                       indexWorkOrders                       = Lists
        .newCopyOnWriteArrayList();
    private List<CollectMetrics>                                                       indexToNodeWorkOrders                 = Lists
        .newCopyOnWriteArrayList();
    private List<CollectMetrics>                                                       nodeWorkOrders                        = Lists
        .newCopyOnWriteArrayList();
    private List<CollectMetrics>                                                       nodeToIndexWorkOrders                 = Lists
        .newCopyOnWriteArrayList();
    private List<CollectMetrics>                                                       ingestWorkOrders                      = Lists
        .newCopyOnWriteArrayList();
    private List<CollectMetrics>                                                       dcdrWorkOrders                        = Lists
        .newCopyOnWriteArrayList();

    /**
     * key：clusterName
     */
    private Map<String, Triple<ESClient, AriusMetaJobClusterDistributePO, ClusterPhy>> localTask                             = new HashMap<>();

    private final Set<String>                                                          notMonitorCluster                     = Sets
        .newHashSet();

    @Value("${monitorJob.threadPool.initsize:20}")
    private int                                                                        poolSize;

    /**
     * maxPoolSize，当前monitorjob能支持的最大集群采集个数，
     * 超过maxPoolSize的集群不会被采集，保证maxPoolSize个集群采集的稳定性
     */
    @Value("${monitorJob.threadPool.maxsize:30}")
    private int                                                                        maxPoolSize;

    private ExecutorService threadPool;

    /**
     * 监控采集任务执行情况
     * key：clusterName
     */
    private Map<String, MonitorTaskInfo>                                               monitorTaskInfoMap                    = Maps
        .newConcurrentMap();

    @PostConstruct
    public void init() {
        indexWorkOrders = MonitorCollectMetrics.initIndexDataRegisterMap();
        nodeWorkOrders = MonitorCollectMetrics.initNodeDataRegisterMap();
        nodeToIndexWorkOrders = MonitorCollectMetrics.initNodeToIndexDataRegisterMap();
        indexToNodeWorkOrders = MonitorCollectMetrics.initIndexToNodeDataRegisterMap(nodeWorkOrders);
        ingestWorkOrders = MonitorCollectMetrics.initIngestDataRegisterMap();
        dcdrWorkOrders = MonitorCollectMetrics.initDCDRDataRegisterMap();
    }

    @Override
    public Object handleBrocastJobTask(String params, String curretnWorker, List<String> allWorkers) {
        if (StringUtils.isEmpty(hostName)) {
            LOGGER.error("class=MonitorJobHandler||method=handleJobTask||hostName is empty!");
            return JOB_FAILED;
        }

        if (CollectionUtils.isEmpty(allWorkers)) {
            LOGGER.error("class=MonitorJobHandler||method=handleJobTask||allWorders is empty!");
            return JOB_FAILED;
        }

        List<ClusterPhy> clusterPhyList = dataSourceFilter(phyClusterService.listAllClusters());
        List<AriusMetaJobClusterDistributePO> allTaskPOList = ariusMetaJobClusterDistributeDAO.getAllTask();

        int jobTotalNu = allWorkers.size();

        //计算本机需要采集的具体es集群
        if (needReAcquireCluster(jobTotalNu, clusterPhyList, allTaskPOList)) {
            List<AriusMetaJobClusterDistributePO> lockedTasks = acquireOwnCluster(jobTotalNu, allTaskPOList);

            localTask.clear();
            localTask = getLocalTaskInfo(lockedTasks, clusterPhyList);
            clusterTotal = jobTotalNu;
        }

        // AMS节点开始采集具体负责的es集群的信息
        if (!localTask.isEmpty()) {
            collectData(localTask);
        }

        return JOB_SUCCESS;
    }

    @Override
    public Object handleJobTask(String params) {
        return JOB_SUCCESS;
    }

    /**************************************** private methods ****************************************/
    private List<ClusterPhy> dataSourceFilter(List<ClusterPhy> allDataSource) {
        List<ClusterPhy> dataSources = new ArrayList<>();

        for (ClusterPhy dataSource : allDataSource) {
            if (notMonitorCluster.contains(dataSource.getCluster())) {
                LOGGER.info("class=MonitorJobHandler||method=dataSourceFilter||cluster={}||msg=not monitor!",
                    dataSource.getCluster());
            } else {
                dataSources.add(dataSource);
            }
        }

        return dataSources;
    }

    /**
     * 是否需要重新把es集群分配到具体的ams节点上
     * @param jobTotalNu
     * @param clusterPhies
     * @param allTaskPOs
     * @return
     */
    private boolean needReAcquireCluster(int jobTotalNu, List<ClusterPhy> clusterPhies,
                                         List<AriusMetaJobClusterDistributePO> allTaskPOs) {
        //allTaskPOs是空，重新分配
        //allTaskPOs发生了变动，重新分配
        if (CollectionUtils.isEmpty(allTaskPOs) || clusterPhies.size() != allTaskPOs.size()) {
            updateMonitorTaskByDataSource(clusterPhies, allTaskPOs);
            return true;
        }

        //localESClient是空，重新分配
        if (localTask.isEmpty()) {
            return true;
        }

        //ams集群数量发生了变更，重新分配
        if (clusterTotal.intValue() != jobTotalNu) {
            return true;
        }

        //30分钟之内没有执行任务，重新分配
        Long now = System.currentTimeMillis();
        for (AriusMetaJobClusterDistributePO taskPO : allTaskPOs) {
            if (now - taskPO.getMonitorTime().getTime() > MONITOR_TASK_CHECK_MINUTES_WITH_MILLS) {
                return true;
            }
        }

        return false;
    }

    /**
     * 在es集群信息发送变动时，根据es集群更新ClusterMonitorTaskEntity
     * @param clusterPhies
     * @param allTaskPOs
     */
    private void updateMonitorTaskByDataSource(List<ClusterPhy> clusterPhies,
                                               List<AriusMetaJobClusterDistributePO> allTaskPOs) {
        Map<Integer, AriusMetaJobClusterDistributePO> deletedCluster = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(allTaskPOs)) {
            for (AriusMetaJobClusterDistributePO taskPO : allTaskPOs) {
                deletedCluster.put(taskPO.getClusterId(), taskPO);
            }
        }

        Map<Integer, ClusterPhy> newCluster = Maps.newHashMap();
        for (ClusterPhy dataSource : clusterPhies) {
            if (deletedCluster.containsKey(dataSource.getId())) {
                deletedCluster.remove(dataSource.getId());
            } else {
                newCluster.put(dataSource.getId(), dataSource);
            }
        }

        List<AriusMetaJobClusterDistributePO> newTasks = Lists.newArrayList();

        for (ClusterPhy dataSource : newCluster.values()) {
            AriusMetaJobClusterDistributePO newTask = new AriusMetaJobClusterDistributePO();
            newTask.setClusterId(dataSource.getId());
            newTask.setCluster(dataSource.getCluster());
            newTask.setDataCentre(dataSource.getDataCenter());
            newTasks.add(newTask);
        }

        LOGGER.info("class=MonitorJobHandler||method=updateMonitorTaskByDataSource||newCluster={}||deletedCluster={}",
            newCluster.keySet(), deletedCluster.keySet());

        if (CollectionUtils.isNotEmpty(newTasks)) {
            for (AriusMetaJobClusterDistributePO taskEntity : newTasks) {
                try {
                    ariusMetaJobClusterDistributeDAO.insert(taskEntity);
                } catch (Exception e) {
                    LOGGER.error(
                        "class=MonitorJobHandler||method=updateMonitorTaskByDataSource||cluster={}||msg=insert taskEntity failed!",
                        taskEntity.getClusterId(), e);
                }
            }
        }

        List<Long> deleteIds = Lists.newArrayList();
        for (AriusMetaJobClusterDistributePO oldTask : deletedCluster.values()) {
            deleteIds.add(oldTask.getId());
        }
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            ariusMetaJobClusterDistributeDAO.deleteBatch(deleteIds);
        }
    }

    /**
     * 开始计算AMS节点具体负责的ClusterMonitorTaskEntity任务，每个节点最多负责maxPoolSize个采集任务
     * @param jobTotalNu
     * @param taskPOs
     * @return
     */
    private List<AriusMetaJobClusterDistributePO> acquireOwnCluster(int jobTotalNu,
                                                                    List<AriusMetaJobClusterDistributePO> taskPOs) {
        Collections.sort(taskPOs, (o1, o2) -> {
            Long o1Time = o1.getMonitorTime().getTime();
            Long o2Time = o2.getMonitorTime().getTime();

            //都没有被monitor
            if (StringUtils.isEmpty(o1.getMonitorHost()) && StringUtils.isEmpty(o2.getMonitorHost())) {
                return o1Time.compareTo(o2Time);
            }

            //都被monitor
            if (!StringUtils.isEmpty(o1.getMonitorHost()) && !StringUtils.isEmpty(o2.getMonitorHost())) {
                if (o1.getMonitorHost().equals(o2.getMonitorHost())) {
                    //优先获取持续被monitor的，保证monitor的稳定性和持续性
                    return o2Time.compareTo(o1Time);
                } else {
                    //都不是本机,不优先
                    if (!o1.getMonitorHost().equals(hostName) && !o2.getMonitorHost().equals(hostName)) {
                        return o1Time.compareTo(o2Time);
                    }

                    //优先本机
                    if (o1.getMonitorHost().equals(hostName)) {
                        return -1;
                    }

                    //优先本机
                    if (o2.getMonitorHost().equals(hostName)) {
                        return 1;
                    }
                }
            }

            //o1是本机monitor
            if (!StringUtils.isEmpty(o1.getMonitorHost()) && o1.getMonitorHost().equals(hostName)) {
                return -1;
            }

            //o2是本机monitor
            if (!StringUtils.isEmpty(o2.getMonitorHost()) && o2.getMonitorHost().equals(hostName)) {
                return 1;
            }

            if (StringUtils.isEmpty(o1.getMonitorHost())) {
                return -1;
            }
            if (StringUtils.isEmpty(o2.getMonitorHost())) {
                return 1;
            }

            return o1Time.compareTo(o2Time);
        });

        Double taskCountCeil = Math.ceil(taskPOs.size() * 1.0 / jobTotalNu);
        int taskCount = taskCountCeil.intValue() > maxPoolSize ? maxPoolSize : taskCountCeil.intValue();

        LOGGER.info("class=MonitorJobHandler||method=acquireOwnCluster||hostName={}||count={}||taskPOs={}||env={}",
            hostName, taskCount, JSON.toJSON(taskPOs), EnvUtil.getStr());

        //顺序遍历列表,尝试抢占任务,每台AMS节点只能抢占taskCount个任务
        List<AriusMetaJobClusterDistributePO> lockedTasks = Lists.newArrayList();
        for (int i = 0; i < taskPOs.size() && taskCount > 0; i++) {
            if (tryLockTask(taskPOs.get(i), hostName)) {
                taskCount--;
                LOGGER.info("class=MonitorJobHandler||method=acquireOwnCluster||hostName={}||task={}", hostName,
                    JSON.toJSONString(taskPOs.get(i)));

                lockedTasks.add(taskPOs.get(i));
            }
        }

        return lockedTasks;
    }

    /**
     * 任务抢占的逻辑
     * @param ariusMetaJobClusterDistributePO
     * @param hostName
     * @return
     */
    private boolean tryLockTask(AriusMetaJobClusterDistributePO ariusMetaJobClusterDistributePO, String hostName) {
        // 某个集群不是本节点负责采集并且据上次monitor时间大于配置的30分钟，则尝试更新数据库
        if (!hostName.equals(ariusMetaJobClusterDistributePO.getMonitorHost())
            && ariusMetaJobClusterDistributePO.getMonitorTime().getTime()
               + MONITOR_TASK_CHECK_MINUTES_WITH_MILLS > System.currentTimeMillis()) {
            LOGGER.info("class=MonitorJobHandler||method=tryLockTask||cluster={}||monitorHostName={}||monitorTime={}",
                ariusMetaJobClusterDistributePO.getCluster(), ariusMetaJobClusterDistributePO.getMonitorHost(),
                ariusMetaJobClusterDistributePO.getMonitorTime().toString());
            return false;
        }

        ariusMetaJobClusterDistributePO.setDestMonitorHost(hostName);
        ariusMetaJobClusterDistributePO.setMonitorTime(new Date());
        ariusMetaJobClusterDistributePO.setGmtModify(new Date());

        //updateMonitorHost不能设置monitorHost
        return ariusMetaJobClusterDistributeDAO.updateMonitorHost(ariusMetaJobClusterDistributePO) == 1;
    }

    private Map<String/*clusterName*/, Triple<ESClient, AriusMetaJobClusterDistributePO, ClusterPhy>> getLocalTaskInfo(List<AriusMetaJobClusterDistributePO> localTasks,
                                                                                                                       List<ClusterPhy> clusterPhies) {
        Map<Integer/*clusterId*/, AriusMetaJobClusterDistributePO> localTaskMap = new HashMap<>(localTasks.size());
        for (AriusMetaJobClusterDistributePO taskPO : localTasks) {
            localTaskMap.put(taskPO.getClusterId(), taskPO);
        }

        List<ClusterPhy> dataSources = clusterPhies.stream()
            .filter(cluster -> localTaskMap.containsKey(cluster.getId())).collect(Collectors.toList());

        Map<String/*clusterName*/, Triple<ESClient, AriusMetaJobClusterDistributePO, ClusterPhy>> stringTripleMap = new HashMap<>(
            dataSources.size());
        for (ClusterPhy esDataSource : dataSources) {
            String cluster = esDataSource.getCluster();
            ESClient esClient = esOpClient.getESClient(cluster);
            if (null != esClient) {
                Integer clusterId = esDataSource.getId();

                stringTripleMap.put(cluster, new Triple<>(esClient, localTaskMap.get(clusterId), esDataSource));
            } else {
                LOGGER.warn("class=MonitorJobHandler||method=getLocalTaskInfo||hostName={}||cluster={}||msg=fail",
                    hostName, cluster);
            }
        }

        LOGGER.info(
            "class=MonitorJobHandler||method=getLocalTaskInfo||hostName={}||taskSize={}||dataSources={}||localTasks={}||env={}",
            hostName, localTasks.size(), dataSources2Str(dataSources), JSON.toJSONString(localTasks), EnvUtil.getStr());

        return stringTripleMap;
    }

    private String dataSources2Str(List<ClusterPhy> dataSources) {
        if (CollectionUtils.isEmpty(dataSources)) {
            return "";
        }

        StringBuilder cluster = new StringBuilder();
        for (ClusterPhy dataSource : dataSources) {
            cluster.append(dataSource.getCluster());
            cluster.append(",");
        }

        return cluster.toString();
    }

    private Map<String/*templateName*/, AmsTemplatePhysicalConfVO> listTypeMappingIndex(String dataCenter) {
        Map<String/*templateName*/, AmsTemplatePhysicalConfVO> resultMap = Maps.newHashMap();
        List<IndexTemplateWithPhyTemplates> logicWithPhysicals = indexTemplateService
            .listTemplateWithPhysical();

        String templateConfig = null;
        AmsTemplatePhysicalConfVO item = null;
        for (IndexTemplateWithPhyTemplates logicWithPhysical : logicWithPhysicals) {
            if (logicWithPhysical.hasPhysicals()) {
                try {
                    templateConfig = logicWithPhysical.getMasterPhyTemplate().getConfig();
                    if (StringUtils.isNotBlank(templateConfig)) {
                        IndexTemplatePhysicalConfig config = JSON.parseObject(templateConfig,
                            IndexTemplatePhysicalConfig.class);

                        if (MapUtils.isNotEmpty(config.getTypeIndexMapping())) {
                            item = new AmsTemplatePhysicalConfVO();
                            item.setLogicId(logicWithPhysical.getId());
                            item.setName(logicWithPhysical.getName());
                            item.setMappingIndexNameEnable(config.getMappingIndexNameEnable());
                            item.setTypeIndexMapping(config.getTypeIndexMapping());

                            resultMap.put(logicWithPhysical.getName(), item);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("method=listTypeMappingIndex||dataCenter={}||errMsg={}", dataCenter, e.getMessage(), e);
                }
            }
        }

        return resultMap;
    }

    /**
     * 获取到已启用索引映射的多type索引信息
     *
     * @param dataCenter
     * @return
     */
    private MulityTypeTemplatesInfo getAllEnabledMulityTypeTemplates(String dataCenter) {
        MulityTypeTemplatesInfo mulityTypeTemplatesInfo = new MulityTypeTemplatesInfo();
        Map<String/*templateName*/, AmsTemplatePhysicalConfVO> result = listTypeMappingIndex(dataCenter);

        if (MapUtils.isEmpty(result)) {
            return mulityTypeTemplatesInfo;
        }

        Map<String/*destTemplateName*/, String/*sourceTemplateName*/> dest2SourceTemplateMap = Maps.newHashMap();
        Map<String/*sourceTemplateName*/, Set<String/*destTemplateName*/>> source2DestTemplateMap = Maps.newHashMap();
        Map<String/*typeName*/, String/*destTemplateName*/> typeIndexMapping = null;

        for (Map.Entry<String/*templateName*/, AmsTemplatePhysicalConfVO> entry : result.entrySet()) {
            if (entry.getValue().getMappingIndexNameEnable().booleanValue()) {
                typeIndexMapping = entry.getValue().getTypeIndexMapping();
                if (MapUtils.isNotEmpty(typeIndexMapping)) {
                    for (Map.Entry<String/*typeName*/, String/*destTemplateName*/> item : typeIndexMapping.entrySet()) {
                        dest2SourceTemplateMap.put(item.getValue(), entry.getKey());
                    }
                    source2DestTemplateMap.put(entry.getKey(), Sets.newHashSet(typeIndexMapping.values()));
                }
            }
        }

        mulityTypeTemplatesInfo.setDest2SourceTemplateMap(dest2SourceTemplateMap);
        mulityTypeTemplatesInfo.setSource2DestTemplateMap(source2DestTemplateMap);

        return mulityTypeTemplatesInfo;
    }

    /**
     * 校验线程资源是否合理
     * @return
     */
    private boolean checkThreadPool() {
        if (threadPool == null || threadPool.isShutdown()) {
            threadPool = Observability.wrap(new ThreadPoolExecutor(poolSize, maxPoolSize + 10, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(100),
                    new BasicThreadFactory.Builder().namingPattern("monitor-cluster-data-collect-%d").build()));
        }

        long blockSize = Optional.ofNullable(ReflectionUtils.findField(ContextExecutorService.class, "delegate")).map(field -> {
            field.setAccessible(true);
            return (ThreadPoolExecutor) ReflectionUtils.getField(field, threadPool);
        }).map(ThreadPoolExecutor::getQueue).map(Collection::size).orElse(0);
        if (blockSize > WARN_BLOCK_SIZE) {
            LOGGER.warn(
                "class=MonitorJobHandler||method=checkThreadPool||blockSize={}||msg=collect thread pool has block task",
                blockSize);
        }

        if (blockSize > ERROR_BLOCK_SIZE) {
            LOGGER.error(
                "class=MonitorJobHandler||method=checkThreadPool||blockSize={}||msg=collect thread pool is too busy. thread pool recreate",
                blockSize);
            threadPool.shutdownNow();
            threadPool = null;
            return false;
        }

        return true;
    }

    /**
     * 采集本机负责的es集群信息
     * @param localTask
     */
    private void collectData(Map<String/*clusterName*/, Triple<ESClient, AriusMetaJobClusterDistributePO, ClusterPhy>> localTask) {
        //清空上个周期的集群缓存, 防止撑爆内存
        metricsRegister.clearComputeValueRegister();

        // 从admin获取所有模板信息
        List<IndexTemplatePhyWithLogic> indexTemplates = indexTemplatePhyService.listTemplateWithLogic();

        Map<String, List<IndexTemplatePhyWithLogic>> indexTemplatesMap = new ConcurrentHashMap<>(16);
        if (CollectionUtils.isNotEmpty(indexTemplates)) {
            indexTemplatesMap
                .putAll(indexTemplates.stream().collect(Collectors.groupingBy(IndexTemplatePhy::getCluster)));
        }

        // 从admin获取所多type的模板信息，包括源模板名和映射到的模板名之间的map
        MulityTypeTemplatesInfo mulityTypeTemplatesInfo = getAllEnabledMulityTypeTemplates("");

        // 遍历集群，进行采集
        localTask.forEach((clusterName, taskEntityTuple) -> {
            try {
                MonitorTaskInfo monitorTaskInfo = monitorTaskInfoMap.computeIfAbsent(clusterName,
                    key -> new MonitorTaskInfo(clusterName, 0L, 0L, false));
                // 如果该集群上一个任务还在运行 并且上个任务开始时间据现在220s(请求超时为50s*4+20s处理时间)内，则返回
                if (monitorTaskInfo.getRunning()
                    && ((System.currentTimeMillis() - monitorTaskInfo.getStartTick()) < TASK_TIMEOUT_TIME)) {
                    LOGGER.warn(
                        "class=MonitorJobHandler||method=collectData||clusterName={}||msg=task is running, monitorTaskInfo {}",
                        clusterName, JSON.toJSONString(monitorTaskInfo));
                    //TODO 未超时的任务先不停掉避免数据时间点缺失
                    //return;
                }

                if (checkThreadPool()) {
                    threadPool.execute(() -> {
                        StopWatch stopWatch = new StopWatch();
                        stopWatch.start("start collect");

                        Long startTime = System.currentTimeMillis();
                        monitorTaskInfo.setRunning(true);
                        monitorTaskInfo.setStartTick(startTime);
                        AriusMetaJobClusterDistributePO taskEntity = taskEntityTuple.v2();

                        Thread.currentThread().setName("monitor-cluster-data-collect-" + clusterName);
                        ESClient esClient = esOpClient.getESClient(clusterName);
                        if (esClient == null) {
                            LOGGER.error(
                                "class=MonitorJobHandler||method=collectData||clusterName={}||errMsg=fail to get esClient",
                                clusterName);
                            monitorTaskInfo.setRunning(false);
                            return;
                        }

                        stopWatch.stop().start("collect");
                        MonitorClusterJob monitorClusterJob = new MonitorClusterJob(esClient, clusterName,
                            taskEntityTuple.v3(),
                            indexTemplatesMap.getOrDefault(clusterName, Lists.newCopyOnWriteArrayList()),
                            metricsRegister, monitorMetricsSender, indexWorkOrders, nodeWorkOrders,
                            indexToNodeWorkOrders, nodeToIndexWorkOrders, ingestWorkOrders, dcdrWorkOrders,
                            mulityTypeTemplatesInfo, ariusConfigInfoService);

                        monitorClusterJob.collectData(clusterName);

                        stopWatch.stop().start("update db");
                        // 更新数据库 任务采集信息
                        taskEntity.setMonitorTime(new Date());
                        taskEntity.setGmtModify(new Date());
                        taskEntity.setMonitorHost(hostName);

                        boolean taskUpdateSuc = ariusMetaJobClusterDistributeDAO.updateMonitorTime(taskEntity) == 1;
                        long totalCost = System.currentTimeMillis() - startTime;

                        monitorTaskInfo.setTotalCost(totalCost);
                        monitorTaskInfo.setRunning(false);

                        LOGGER.info(
                            "class=MonitorJobHandler||method=collectData||clusterName={}||cost={}||taskUpdateSuc={}||host={}||taskEntity={}||env={}",
                            clusterName, stopWatch.toString(), taskUpdateSuc, hostName, JSON.toJSONString(taskEntity),
                            EnvUtil.getStr());
                    });
                }
            } catch (Exception e) {
                LOGGER.error(
                    "class=MonitorJobHandler||method=collectData||clusterName={}||host={}||env={}||msg=excepiton",
                    clusterName, hostName, EnvUtil.getStr(), e);
            }
        });
    }
}