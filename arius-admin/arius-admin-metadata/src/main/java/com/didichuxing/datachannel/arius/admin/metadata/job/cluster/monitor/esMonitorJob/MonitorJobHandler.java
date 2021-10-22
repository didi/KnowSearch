package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.AmsTemplatePhysicalConfVO;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.MulityTypeTemplatesInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.MonitorTaskInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.ClusterMonitorTaskPO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpHostUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.metrics.CollectMetrics;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.metrics.MetricsRegister;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor.ClusterMonitorTaskDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_FAILED;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;


/**
 * 通过查数据库观察每个节点采集es集群名称
 *
 * select * from cluster_monitor_task_v2 where monitor_time > '2020-07-10 15:01:10' and dataCentre='cn' order by monitor_host desc  limit 20
 */
@Component
public class MonitorJobHandler extends AbstractMetaDataJob {

    @Autowired
    private ClusterMonitorTaskDAO clusterMonitorTaskDAO;

    @Autowired
    private ESClusterPhyService      phyClusterService;

    @Autowired
    private TemplatePhyService templatePhyService;

    @Autowired
    private TemplateLogicService templateLogicService;

    @Autowired
    private ESOpClient               esOpClient;

    @Autowired
    private MonitorMetricsSender     monitorMetricsSender;

    private MetricsRegister             metricsRegister = new MetricsRegister();

    private String  hostName     = HttpHostUtil.HOST_NAME;
    private Integer clusterTotal = -1;

    private String dataCentre    = EnvUtil.getDC().getCode();

    private static final Long  MONITOR_TASK_CHECK_MINUTES_WITH_MILLS = EnvUtil.isOnline() ? (long) (1000 * 60 * 30) : (long) (1000 * 60 * 5);

    /**
     * 采集指标
     */
    private CopyOnWriteArrayList<CollectMetrics> indexWorkOrders       = Lists.newCopyOnWriteArrayList();
    private CopyOnWriteArrayList<CollectMetrics> indexToNodeWorkOrders = Lists.newCopyOnWriteArrayList();
    private CopyOnWriteArrayList<CollectMetrics> nodeWorkOrders        = Lists.newCopyOnWriteArrayList();
    private CopyOnWriteArrayList<CollectMetrics> nodeToIndexWorkOrders = Lists.newCopyOnWriteArrayList();
    private CopyOnWriteArrayList<CollectMetrics> ingestWorkOrders      = Lists.newCopyOnWriteArrayList();
    private CopyOnWriteArrayList<CollectMetrics> dcdrWorkOrders        = Lists.newCopyOnWriteArrayList();

    private Map<String/*clusterName*/, Triple<ESClient, ClusterMonitorTaskPO, ESClusterPhy>> localTask = new HashMap<>();

    private Set<String> notMonitorCluster = Sets.newHashSet();

    private ThreadPoolExecutor threadPool;

    /**
     * 监控采集任务执行情况
     */
    private Map<String/*clusterName*/, MonitorTaskInfo> monitorTaskInfoMap = Maps.newConcurrentMap();

    @PostConstruct
    public void init(){
        indexWorkOrders       = MonitorCollectMetrics.initIndexDataRegisterMap();
        nodeWorkOrders        = MonitorCollectMetrics.initNodeDataRegisterMap();
        nodeToIndexWorkOrders = MonitorCollectMetrics.initNodeToIndexDataRegisterMap();
        indexToNodeWorkOrders = MonitorCollectMetrics.initIndexToNodeDataRegisterMap(nodeWorkOrders);
        ingestWorkOrders      = MonitorCollectMetrics.initIngestDataRegisterMap();
        dcdrWorkOrders        = MonitorCollectMetrics.initDCDRDataRegisterMap();
    }

    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=MonitorJobHandler||method=handleJobTask||params={}||env={}", params, EnvUtil.getStr());

        if (StringUtils.isEmpty(hostName)) {
            LOGGER.error("class=MonitorJobHandler||method=handleJobTask||hostName is empty!");
            return JOB_FAILED;
        }

        //获取es集群信息、tts调度shard信息、当前AMS各个节点分配的es集群信息
//        ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();
//        if (null == shardingVO) {
//            LOGGER.error("class=MonitorJobHandler||method=handleJobTask||tts sharding is null!");
//            return null;
//        }

        List<ESClusterPhy>         esClusterPhys  = dataSourceFilter(phyClusterService.listAllClusters());
        List<ClusterMonitorTaskPO> allTaskPOs     = clusterMonitorTaskDAO.getAllTaskByDataCentre(dataCentre);

//        int jobTotalNu = shardingVO.getTotal();
        int jobTotalNu = 1;

        //计算本机需要采集的具体es集群
        if (needReAcquireCluster(jobTotalNu, esClusterPhys, allTaskPOs)) {
            allTaskPOs = clusterMonitorTaskDAO.getAllTaskByDataCentre(dataCentre);
            List<ClusterMonitorTaskPO> lockedTasks = acquireOwnCluster(jobTotalNu, allTaskPOs);

            localTask.clear();
            localTask     = getLocalTaskInfo(lockedTasks, esClusterPhys);
            clusterTotal  = jobTotalNu;
        }

        // AMS节点开始采集具体负责的es集群的信息
        if (localTask.isEmpty() == false) {
            collectData(localTask);
        }

        return JOB_SUCCESS;
    }

    /**
     * 测试使用，能够根据名称采集监控信息
     * @param cluster 集群名称
     */
    public void monitorCluster(String cluster) {
        List<ClusterMonitorTaskPO> lockedTasks = new ArrayList<>();

        List<ESClusterPhy>             esClusterPhys   = dataSourceFilter(phyClusterService.listAllClusters());
        List<ClusterMonitorTaskPO> allTaskPOs      = clusterMonitorTaskDAO.getAllTaskByDataCentre(dataCentre);

        for (ClusterMonitorTaskPO clusterMonitorTaskPO : allTaskPOs) {
            if (clusterMonitorTaskPO.getCluster().equals(cluster)) {
                lockedTasks.add( clusterMonitorTaskPO );
                break;
            }
        }

        localTask = getLocalTaskInfo(lockedTasks, esClusterPhys);

        collectData(localTask);
    }

    /**************************************** private methods ****************************************/
    private List<ESClusterPhy> dataSourceFilter(List<ESClusterPhy> allDataSource){
        List<ESClusterPhy> dataSources = new ArrayList<>();

        for(ESClusterPhy dataSource : allDataSource){
            if (notMonitorCluster.contains(dataSource.getCluster())) {
                LOGGER.info("class=MonitorJobHandler||method=dataSourceFilter||cluster={}||msg=not monitor!",
                        dataSource.getCluster());
                continue;
            }else {
                dataSources.add(dataSource);
            }
        }

        return dataSources;
    }

    /**
     * 是否需要重新把es集群分配到具体的ams节点上
     * @param jobTotalNu
     * @param esClusterPhys
     * @param allTaskPOs
     * @return
     */
    private boolean needReAcquireCluster(int jobTotalNu,
                                         List<ESClusterPhy> esClusterPhys,
                                         List<ClusterMonitorTaskPO> allTaskPOs){
        //allTaskPOs是空，重新分配
        //allTaskPOs发生了变动，重新分配
        if (CollectionUtils.isEmpty(allTaskPOs) ||
                esClusterPhys.size() != allTaskPOs.size()){
            updateMonitorTaskByDataSource(esClusterPhys, allTaskPOs);
            return true;
        }

        //localESClient是空，重新分配
        if (localTask.isEmpty()){return true;}

        //ams集群数量发生了变更，重新分配
        if (clusterTotal.intValue() != jobTotalNu){return true;}

        //30分钟之内没有执行任务，重新分配
        Long now = System.currentTimeMillis();
        for (ClusterMonitorTaskPO taskPO : allTaskPOs) {
            if (now - taskPO.getMonitorTime().getTime() > MONITOR_TASK_CHECK_MINUTES_WITH_MILLS) {
                return true;
            }
        }

        return false;
    }

    /**
     * 在es集群信息发送变动时，根据es集群更新ClusterMonitorTaskEntity
     * @param esClusterPhys
     * @param allTaskPOs
     */
    private void updateMonitorTaskByDataSource(List<ESClusterPhy> esClusterPhys, List<ClusterMonitorTaskPO> allTaskPOs) {
        Map<Integer, ClusterMonitorTaskPO> deletedCluster = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(allTaskPOs)) {
            for (ClusterMonitorTaskPO taskPO : allTaskPOs) {
                deletedCluster.put(taskPO.getClusterId(), taskPO);
            }
        }

        Map<Integer, ESClusterPhy> newCluster = Maps.newHashMap();
        for (ESClusterPhy dataSource : esClusterPhys) {
            if (deletedCluster.containsKey(dataSource.getId())) {
                deletedCluster.remove(dataSource.getId());
            } else {
                newCluster.put(dataSource.getId(), dataSource);
            }
        }

        List<ClusterMonitorTaskPO> newTasks = Lists.newArrayList();

        for (ESClusterPhy dataSource : newCluster.values()) {
            ClusterMonitorTaskPO newTask = new ClusterMonitorTaskPO();
            newTask.setClusterId(dataSource.getId());
            newTask.setCluster(dataSource.getCluster());
            newTask.setDataCentre(dataSource.getDataCenter());
            newTasks.add(newTask);
        }

        LOGGER.info("class=MonitorJobHandler||method=updateMonitorTaskByDataSource||newCluster={}||deletedCluster={}",
                newCluster.keySet(), deletedCluster.keySet());

        if (CollectionUtils.isNotEmpty(newTasks)) {
            for (ClusterMonitorTaskPO taskEntity : newTasks) {
                try {
                    clusterMonitorTaskDAO.insert(taskEntity);
                } catch (Exception e) {
                    LOGGER.error("class=MonitorJobHandler||method=updateMonitorTaskByDataSource||cluster={}||msg=insert taskEntity failed!",
                            taskEntity.getClusterId(), e);
                }
            }
        }

        List<Long> deleteIds = Lists.newArrayList();
        for (ClusterMonitorTaskPO oldTask : deletedCluster.values()) {
            deleteIds.add(oldTask.getId());
        }
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            clusterMonitorTaskDAO.deleteBatch(deleteIds);
        }
    }

    /**
     * 开始计算AMS节点具体负责的ClusterMonitorTaskEntity任务
     * @param jobTotalNu
     * @param taskPOs
     * @return
     */
    private List<ClusterMonitorTaskPO> acquireOwnCluster(int jobTotalNu, List<ClusterMonitorTaskPO> taskPOs) {
        Collections.sort(taskPOs, (o1, o2) -> {
            Long o1Time = o1.getMonitorTime().getTime();
            Long o2Time = o2.getMonitorTime().getTime();

            //都没有被monitor
            if (StringUtils.isEmpty(o1.getMonitorHost()) && StringUtils.isEmpty(o2.getMonitorHost())){
                return o1Time.compareTo(o2Time);
            }

            //都被monitor
            if(!StringUtils.isEmpty(o1.getMonitorHost()) && !StringUtils.isEmpty(o2.getMonitorHost())){
                if(o1.getMonitorHost().equals(o2.getMonitorHost())){
                    //优先获取长时间没有被monitor的
                    return o1Time.compareTo(o2Time);
                }else{
                    //都不是本机,不优先
                    if(!o1.getMonitorHost().equals(hostName)
                            && !o2.getMonitorHost().equals(hostName)){
                        return o1Time.compareTo(o2Time);
                    }

                    //优先本机
                    if (o1.getMonitorHost().equals(hostName)){return -1;}

                    //优先本机
                    if (o2.getMonitorHost().equals(hostName)){return 1;}
                }
            }

            //o1是本机monitor
            if (!StringUtils.isEmpty(o1.getMonitorHost()) && o1.getMonitorHost().equals(hostName)){
                return -1;
            }

            //o2是本机monitor
            if (!StringUtils.isEmpty(o2.getMonitorHost()) && o2.getMonitorHost().equals(hostName)){
                return 1;
            }

            if (StringUtils.isEmpty(o1.getMonitorHost())){return -1;}
            if (StringUtils.isEmpty(o2.getMonitorHost())){return 1;}

            return o1Time.compareTo(o2Time);
        });

        Double taskCountCeil = Math.ceil(taskPOs.size() * 1.0 / jobTotalNu);
        int taskCount = taskCountCeil.intValue();

        LOGGER.info("class=MonitorJobHandler||method=acquireOwnCluster||hostName={}||count={}||taskPOs={}||env={}",
                hostName, taskCount, JSON.toJSON(taskPOs), EnvUtil.getStr());

        //顺序遍历列表,尝试抢占任务,每台AMS节点只能抢占taskCount个任务
        List<ClusterMonitorTaskPO> lockedTasks = Lists.newArrayList();
        for (int i = 0; i < taskPOs.size() && taskCount > 0; i++) {
            if (tryLockTask(taskPOs.get(i), hostName)) {
                taskCount--;
                LOGGER.info("class=MonitorJobHandler||method=acquireOwnCluster||hostName={}||task={}",
                        hostName, JSON.toJSONString(taskPOs.get(i)));

                lockedTasks.add(taskPOs.get(i));
            }
        }

        return lockedTasks;
    }

    /**
     * 任务抢占的逻辑
     * @param clusterMonitorTaskPO
     * @param hostName
     * @return
     */
    private boolean tryLockTask(ClusterMonitorTaskPO clusterMonitorTaskPO, String hostName) {
        // 某个集群不是本节点负责采集并且据上次monitor时间大于配置的30分钟，则尝试更新数据库
        if (!hostName.equals(clusterMonitorTaskPO.getMonitorHost())
                && clusterMonitorTaskPO.getMonitorTime().getTime() + MONITOR_TASK_CHECK_MINUTES_WITH_MILLS > System.currentTimeMillis()) {
            LOGGER.info("class=MonitorJobHandler||method=tryLockTask||cluster={}||monitorHostName={}||monitorTime={}",
                    clusterMonitorTaskPO.getCluster(), clusterMonitorTaskPO.getMonitorHost(), clusterMonitorTaskPO.getMonitorTime().toString());
            return false;
        }

        clusterMonitorTaskPO.setDestMonitorHost(hostName);
        clusterMonitorTaskPO.setMonitorTime(new Date());
        clusterMonitorTaskPO.setGmtModify(new Date());

        //updateMonitorHost不能设置monitorHost
        return clusterMonitorTaskDAO.updateMonitorHost(clusterMonitorTaskPO) == 1;
    }

    private Map<String/*clusterName*/, Triple<ESClient, ClusterMonitorTaskPO, ESClusterPhy>> getLocalTaskInfo (
            List<ClusterMonitorTaskPO> localTasks, List<ESClusterPhy> esClusterPhys){
        Map<Integer/*clusterId*/, ClusterMonitorTaskPO> localTaskMap = new HashMap<>();
        for (ClusterMonitorTaskPO taskPO : localTasks) {
            localTaskMap.put(taskPO.getClusterId(), taskPO);
        }

        List<ESClusterPhy> dataSources = esClusterPhys.stream().filter(cluster -> localTaskMap.containsKey(cluster.getId())).collect(Collectors.toList());

        Map<String/*clusterName*/, Triple<ESClient, ClusterMonitorTaskPO, ESClusterPhy>> stringTripleMap = new HashMap<>();
        for (ESClusterPhy esDataSource : dataSources) {
            String   cluster   = esDataSource.getCluster();
            ESClient esClient = esOpClient.getESClient(cluster);
            if (null != esClient) {
                Integer clusterId   = esDataSource.getId();

                stringTripleMap.put(cluster, new Triple<>(esClient, localTaskMap.get(clusterId), esDataSource));
            } else {
                LOGGER.warn("class=MonitorJobHandler||method=getLocalTaskInfo||hostName={}||cluster={}||msg=fail",
                        hostName, cluster);
            }
        }

        LOGGER.info("class=MonitorJobHandler||method=getLocalTaskInfo||hostName={}||taskSize={}||dataSources={}||localTasks={}||env={}",
                hostName, localTasks.size(), dataSources2Str(dataSources), JSON.toJSONString(localTasks), EnvUtil.getStr());

        return stringTripleMap;
    }

    private String dataSources2Str(List<ESClusterPhy> dataSources){
        if (CollectionUtils.isEmpty(dataSources)) {return "";}

        String cluster = "";
        for (ESClusterPhy dataSource : dataSources) {
            cluster += dataSource.getCluster();
            cluster += ",";
        }

        return cluster;
    }

    private Map<String/*templateName*/, AmsTemplatePhysicalConfVO> listTypeMappingIndex(String dataCenter) {
        Map<String/*templateName*/, AmsTemplatePhysicalConfVO> resultMap = Maps.newHashMap();
        List<IndexTemplateLogicWithPhyTemplates> logicWithPhysicals = templateLogicService
                .getTemplateWithPhysicalByDataCenter(dataCenter);

        String templateConfig = null;
        AmsTemplatePhysicalConfVO item = null;
        for (IndexTemplateLogicWithPhyTemplates logicWithPhysical : logicWithPhysicals) {
            if (logicWithPhysical.hasPhysicals()) {
                try {
                    templateConfig = logicWithPhysical.getAnyOne().getConfig();
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

        Map<String/*destTemplateName*/, String/*sourceTemplateName*/> dest2SourceTemplateMap =  Maps.newHashMap();
        Map<String/*sourceTemplateName*/, Set<String/*destTemplateName*/>> source2DestTemplateMap = Maps.newHashMap();
        Map<String/*typeName*/, String/*destTemplateName*/> typeIndexMapping = null;

        for (Map.Entry<String/*templateName*/, AmsTemplatePhysicalConfVO> entry : result.entrySet()) {
            if (entry.getValue().getMappingIndexNameEnable()) {
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
            threadPool = new ThreadPoolExecutor(10, 20,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>( 100 ),
                    new BasicThreadFactory.Builder().namingPattern("monitor-cluster-data-collect-%d").build());
        }

        long blockSize = threadPool.getQueue().size();
        if (blockSize > 10) {
            LOGGER.warn("class=MonitorJobHandler||method=checkThreadPool||blockSize={}||msg=collect thread pool has block task", blockSize);
        }

        if (blockSize > 30) {
            LOGGER.error("class=MonitorJobHandler||method=checkThreadPool||blockSize={}||msg=collect thread pool is too busy. thread pool recreate", blockSize);
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
    private void collectData(Map<String/*clusterName*/, Triple<ESClient, ClusterMonitorTaskPO, ESClusterPhy>> localTask) {
        //清空上个周期的集群缓存, 防止撑爆内存
        metricsRegister.clearComputeValueRegister();

        // 从admin获取所有模板信息
        List<IndexTemplatePhyWithLogic> indexTemplates = templatePhyService.listTemplateWithLogicWithCache();

        // 从admin获取所多type的模板信息，包括源模板名和映射到的模板名之间的map
        MulityTypeTemplatesInfo mulityTypeTemplatesInfo = getAllEnabledMulityTypeTemplates("");

        // 遍历集群，进行采集
        localTask.forEach((clusterName, taskEntityTuple) -> {

            MonitorTaskInfo monitorTaskInfo = monitorTaskInfoMap.computeIfAbsent(clusterName, key -> new MonitorTaskInfo(clusterName, 0L, 0L, false));
            // 如果该集群上一个任务还在运行 并且上个任务开始时间据现在220s(请求超时为50s*4+20s处理时间)内，则返回
            if (monitorTaskInfo.getRunning() && ((System.currentTimeMillis() - monitorTaskInfo.getStartTick()) < 220 * 1000L)) {
                LOGGER.warn("class=MonitorJobHandler||method=collectData||clusterName={}||msg=task is running, monitorTaskInfo {}", clusterName, JSON.toJSONString(monitorTaskInfo));
                return;
            }

            if (checkThreadPool()) {
                threadPool.execute(
                        () -> {
                            Long startTime = System.currentTimeMillis();
                            monitorTaskInfo.setRunning(true);
                            monitorTaskInfo.setStartTick(startTime);
                            ClusterMonitorTaskPO taskEntity = taskEntityTuple.v2();

                            Thread.currentThread().setName("monitor-cluster-data-collect-" + clusterName);
                            ESClient esClient = esOpClient.getESClient(clusterName);
                            if (esClient == null) {
                                int effectCount = clusterMonitorTaskDAO.deleteBatch(Lists.newArrayList(taskEntity.getId()));
                                LOGGER.error("class=MonitorJobHandler||method=collectData||clusterName={}||errMsg=fail to get esClient, then delete it result {}", clusterName, effectCount);
                                monitorTaskInfo.setRunning(false);
                                return;
                            }
                            MonitorClusterJob monitorClusterJob = new MonitorClusterJob(
                                    esClient,
                                    taskEntityTuple.v3(),
                                    indexTemplates,
                                    metricsRegister,
                                    monitorMetricsSender,
                                    indexWorkOrders,
                                    nodeWorkOrders,
                                    indexToNodeWorkOrders,
                                    nodeToIndexWorkOrders,
                                    ingestWorkOrders,
                                    dcdrWorkOrders,
                                    mulityTypeTemplatesInfo);

                            monitorClusterJob.collectData();

                            // 更新数据库 任务采集信息
                            taskEntity.setMonitorTime(new Date());
                            taskEntity.setGmtModify(new Date());
                            taskEntity.setMonitorHost(hostName);

                            boolean taskUpdateSuc = clusterMonitorTaskDAO.updateMonitorTime(taskEntity) == 1;
                            long totalCost = System.currentTimeMillis() - startTime;

                            LOGGER.info("class=MonitorJobHandler||method=collectData||clusterName={}||cost={}||taskUpdateSuc={}||host={}||taskEntity={}||env={}",
                                    clusterName, totalCost, taskUpdateSuc, hostName, JSON.toJSONString(taskEntity), EnvUtil.getStr());

                            monitorTaskInfo.setTotalCost(totalCost);
                            monitorTaskInfo.setRunning(false);
                        });
            }
        });
    }
}
