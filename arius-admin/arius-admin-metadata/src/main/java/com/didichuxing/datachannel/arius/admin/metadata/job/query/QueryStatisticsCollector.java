package com.didichuxing.datachannel.arius.admin.metadata.job.query;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.LongAdder;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayJoinPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.AppIdTemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.IndexNameAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.QueryStatisticsResult;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.TemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.app.AppIdTemplateAccessESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayJoinESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index.IndexAccessESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateAccessESDAO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.*;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/26 下午9:05
 * @modified By D10865
 *
 * 查询统计(索引模板维度，索引名称维度，appid维度访问统计次数)
 *
 */
@Component
public class QueryStatisticsCollector extends AbstractMetaDataJob {

    @Autowired
    private TemplatePhyService templatePhyService;

    @Autowired
    private ESIndexService  esIndexService;

    @Autowired
    private GatewayJoinESDAO gatewayJoinEsDao;

    @Autowired
    private AppIdTemplateAccessESDAO appIdTemplateAccessCountEsDao;

    @Autowired
    private IndexAccessESDAO indexAccessESDao;

    @Autowired
    private TemplateAccessESDAO templateAccessEsDao;

    /**
     * 索引模板
     */
    private Map<String/*templateName*/, List<IndexTemplatePhyWithLogic>> indexTemplateMap = null;
    /**
     * 索引模板
     */
    private List<IndexTemplatePhyWithLogic> indexTemplateList = null;
    /**
     * 一天记录数
     */
    private LongAdder totalCount = new LongAdder();
    /**
     * 没有访问记录的索引模板名称
     */
    private Set<String/*templateName*/> noAccessTemplateNames = Sets.newLinkedHashSet();

    /**
     * 跳过特殊的索引名称
     */
    private static final Set<String> skipIndexNameSet = Sets.newHashSet("_aliases", "_mapping", ".arius_info",
            "logstash-*/_mapping/field/*", "_search/scroll", "_sql/explain", "_cluster/health");

    private static final FutureUtil<Map<Integer, Map<String, LongAdder>>> futureUtil = FutureUtil.init("QueryStatisticsCollector",10,20,100);

    private static final Integer TIMEOUT_SECOND = 60 * 60 * 2;
    /**
     * 处理任务
     *
     * @param params 参数
     * @return
     */
    @Override
    public Object handleJobTask(String params) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("current run task");

        LOGGER.info("class=QueryStatisticsCollector||method=handleJobTask||params={}", params);
        String date = params;

        if (StringUtils.isBlank(date)) {
            date = DateTimeUtil.getFormatDayByOffset(1);
        }

        indexTemplateList = templatePhyService.listTemplateWithLogic();
        if (CollectionUtils.isNotEmpty(indexTemplateList)) {
            indexTemplateMap = Maps.newConcurrentMap();
            for (IndexTemplatePhyWithLogic indexTemplate : indexTemplateList) {
                indexTemplateMap.computeIfAbsent(indexTemplate.getName(), templateName -> Lists.newArrayList()).add(indexTemplate);
            }
        }

        if (indexTemplateMap == null || indexTemplateList == null) {
            LOGGER.error("class=QueryStatisticsCollector||method=handleJobTask||errMsg=index template response is empty");
            return JOB_FAILED;
        }

        // 运行任务获取结果
        Map<Integer/*appId*/, Map<String/*indexName*/, Long/*access indexName count*/>> map = runTaskAndGetResult(date);
        stopWatch.stop().start("statistic result");

        LOGGER.info("class=QueryStatisticsCollector||method=handleJobTask||totalCount={}||accessInfoMap={}", totalCount.longValue(), JSON.toJSONString(map));

        noAccessTemplateNames.clear();
        // 进行不同维度的统计
        QueryStatisticsResult queryStatisticsResult = statisticsAppIdAccessDetailInfo(map, date);
        if (queryStatisticsResult == null) {
            LOGGER.error("class=QueryStatisticsCollector||method=handleJobTask||errMsg=query statistics result is null");
            return JOB_FAILED;
        }

        boolean result1 = appIdTemplateAccessCountEsDao.batchInsert(Lists.newArrayList(queryStatisticsResult.getAppIdTemplateAccessCountMap().values()));
        boolean result2 = indexAccessESDao.batchInsert(Lists.newArrayList(queryStatisticsResult.getIndexNameAccessCountMap().values()));
        boolean result3 = templateAccessEsDao.batchInsert(Lists.newArrayList(queryStatisticsResult.getTemplateAccessCountMap().values()));

        stopWatch.stop();

        LOGGER.info("class=QueryStatisticsCollector||method=handleJobTask||msg=result1 {}, result2 {}, result3 {}, cost {}",
                result1, result2, result3, stopWatch.toString());

        return JOB_SUCCESS;
    }

    /**
     * 获取查询使用的索引所在集群
     *
     * @param indexTemplateMap
     * @return
     */
    private String getSearchTemplateClusterName(String templateName, Map<String/*templateName*/, List<IndexTemplatePhyWithLogic>> indexTemplateMap) {
        List<IndexTemplatePhyWithLogic> indexTemplatePhyWithLogics = indexTemplateMap.get(templateName);
        if (CollectionUtils.isEmpty(indexTemplatePhyWithLogics)) {
            return null;
        }

        return indexTemplatePhyWithLogics.get(0).getCluster();
    }

    /**
     * 运行任务获取结果
     *
     * @param date
     * @return
     */
    private Map<Integer/*appId*/, Map<String/*indexName*/, Long/*access indexName count*/>> runTaskAndGetResult(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (StringUtils.isBlank(date)) {
            date = dateTimeFormatter.format(ZonedDateTime.now().minus(1, ChronoUnit.DAYS));
        }

        totalCount.reset();

        // 得到对应索引的主shard个数
        String gatewayJoinIndexName     = gatewayJoinEsDao.getIndex(date);
        String gatewayJoinTemplateName  = gatewayJoinEsDao.getTemplateName();
        String gatewayJoinClusterName   = getSearchTemplateClusterName(gatewayJoinTemplateName, indexTemplateMap);

        if(StringUtils.isBlank(gatewayJoinClusterName)){return null;}

        // 获取索引的主shard个数
        Integer primaryShardNumber = esIndexService.syncGetIndexPrimaryShardNumber(gatewayJoinClusterName, String.format("%s*", gatewayJoinIndexName));

        if (primaryShardNumber == null) {
            LOGGER.error("class=QueryStatisticsCollector||method=runTaskAndGetResult||errMsg=clusterName {}, indexName {} primaryShardNumber is null",
                    gatewayJoinClusterName, gatewayJoinIndexName);
            primaryShardNumber = 40;
        }
        // 按shard个数任务切分
        for (int i = 0; i < primaryShardNumber; ++i) {
            futureUtil.callableTask(new QueryStatisticsCollectorCallable(date, i));
        }

        LOGGER.info("class=QueryStatisticsCollector||method=runTaskAndGetResult||msg=submit {} tasks", primaryShardNumber);

        Map<Integer/*appId*/, Map<String/*indexName*/, Long/*access indexName count*/>> map = Maps.newTreeMap();

        List<Map<Integer/*appId*/, Map<String/*indexName*/, LongAdder/*access indexName count*/>>> rets = futureUtil.waitResult(TIMEOUT_SECOND);

        // 汇聚每个shard统计的结果
        collectShardCountResult(map, rets);

        return map;
    }

    private void collectShardCountResult(Map<Integer, Map<String, Long>> map, List<Map<Integer, Map<String, LongAdder>>> rets) {
        String gatewayJoinIndexName;
        Map<String/*indexName*/, Long/*access indexName count*/> accessMap;
        for(Map<Integer/*appId*/, Map<String/*indexName*/, LongAdder/*access indexName count*/>> r : rets){
            if (r != null) {
                for (Map.Entry<Integer/*appId*/, Map<String/*indexName*/, LongAdder/*access indexName count*/>> entry : r.entrySet()) {

                    accessMap = map.computeIfAbsent(entry.getKey(), appId -> Maps.newLinkedHashMap());

                    for (Map.Entry<String/*indexName*/, LongAdder/*access indexName count*/> subEntry : entry.getValue().entrySet()) {
                        gatewayJoinIndexName = subEntry.getKey();
                        accessMap.putIfAbsent(gatewayJoinIndexName, 0L);

                        // 累加索引的访问次数
                        accessMap.put(gatewayJoinIndexName,
                                accessMap.get(gatewayJoinIndexName) + subEntry.getValue().longValue());

                    }
                }
            } else {
                LOGGER.error("class=QueryStatisticsCollector||method=runTaskAndGetResult||errMsg=future result is empty");
            }
        }
    }

    /**
     * 进行不同维度的统计
     *
     * @param map
     * @return
     */
    private QueryStatisticsResult statisticsAppIdAccessDetailInfo(Map<Integer/*appId*/, Map<String/*indexName*/, Long/*access indexName count*/>> map, String date) {
        QueryStatisticsResult stResult = new QueryStatisticsResult();
        /**
         *  索引模板维度访问次数
         */
        Map<String/*templateId*/, TemplateAccessCountPO> templateAccessCountMap = stResult.getTemplateAccessCountMap();
        /**
         * 索引维度访问次数
         */
        Map<String/*templateId_indexName*/, IndexNameAccessCountPO> indexNameAccessCountMap = stResult.getIndexNameAccessCountMap();
        /**
         * appid维度访问次数
         */
        Map<String/*templateId_appId*/, AppIdTemplateAccessCountPO> appIdTemplateAccessCountMap = stResult.getAppIdTemplateAccessCountMap();

        Integer appId;
        String indexName;

        Set<String> matchIndexTemplateNameSet;

        IndexTemplatePhyWithLogic  indexTemplate;

        TemplateAccessCountPO      templateAccessCountPo;
        IndexNameAccessCountPO     indexNameAccessCountPO;
        AppIdTemplateAccessCountPO appIdTemplateAccessCountPO;

        Long accessIndexCount;
        Long noAccessTemplateCount = 0L;

        // 缓存已经匹配过的索引对应的索引模板名
        Map<String/*indexName*/, Set<String/*match template name*/>> matchIndexTemplateCacheMap = Maps.newHashMap();

        for (Map.Entry<Integer/*appId*/, Map<String/*indexName*/, Long/*access indexName count*/>> entry : map.entrySet()) {

            appId = entry.getKey();

            for (Map.Entry<String/*indexName*/, Long/*access indexName count*/> subEntry : entry.getValue().entrySet()) {
                indexName = subEntry.getKey();
                accessIndexCount = subEntry.getValue();

                // 忽略特殊索引名称
                if (skipIndexNameSet.contains(indexName)) {
                    continue;
                }
                if (StringUtils.isNoneBlank(indexName) && indexName.startsWith(".kibana")) {
                    continue;
                }

                if (matchIndexTemplateCacheMap.containsKey(indexName)) {
                    matchIndexTemplateNameSet = matchIndexTemplateCacheMap.get(indexName);
                } else {
                    // 根据索引名称找到对应索引模板
                    matchIndexTemplateNameSet = IndexNameUtils.matchIndexTemplateBySearchIndexName(indexName, indexTemplateList);
                    matchIndexTemplateCacheMap.put(indexName, matchIndexTemplateNameSet);

                    if (matchIndexTemplateNameSet.isEmpty()) {
                        LOGGER.error("class=QueryStatisticsCollector||method=statisticsAppIdAccessDetailInfo||errMsg=appId -> [{}], indexName -> [{}] not match any indexTemplate or alias", appId, indexName);
                        continue;

                    } else if (matchIndexTemplateNameSet.size() > 1) {
                        LOGGER.error("class=QueryStatisticsCollector||method=statisticsAppIdAccessDetailInfo||errMsg=appId -> [{}], indexName -> [{}] match more indexTemplate or alias, count {}, {}", appId, indexName,
                                matchIndexTemplateNameSet.size(), matchIndexTemplateNameSet);
                        continue;
                    } else {
                        LOGGER.info("class=QueryStatisticsCollector||method=statisticsAppIdAccessDetailInfo||errMsg=appId -> [{}], indexName -> [{}] match indexTemplate or alias {}", appId, indexName,
                                matchIndexTemplateNameSet);
                    }
                }

                if (matchIndexTemplateNameSet.isEmpty() || matchIndexTemplateNameSet.size() > 1) {
                    continue;
                }

                // 索引模式下查询只能匹配到1个索引模板
                for (String templateName : matchIndexTemplateNameSet) {

                    List<IndexTemplatePhyWithLogic> templates = indexTemplateMap.get(templateName);
                    indexTemplate = null;
                    if (CollectionUtils.isNotEmpty(templates)) {
                        indexTemplate = templates.get(0);
                    }
                    if (indexTemplate == null) {
                        LOGGER.error("class=QueryStatisticsCollector||method=statisticsAppIdAccessDetailInfo||errMsg=indexName -> [{}], appId {}, not match {}",
                                indexName, appId, templateName);
                        continue;
                    }

                    Integer indexTemplateId = indexTemplate.getId().intValue();
                    Integer logicTemplateId = indexTemplate.getLogicId();
                    String  clusterName     = indexTemplate.getCluster();
                    String  indexTemplateName = indexTemplate.getName();

                    templateAccessCountPo = templateAccessCountMap.computeIfAbsent(String.valueOf(indexTemplateId), templateId -> {
                        TemplateAccessCountPO po = new TemplateAccessCountPO();
                        po.setTemplateId(indexTemplateId).setLogicTemplateId(logicTemplateId).setClusterName(clusterName).setTemplateName(indexTemplateName).setDate(date);
                        return po;
                    });
                    templateAccessCountPo.increase(accessIndexCount);

                    indexNameAccessCountPO = indexNameAccessCountMap.computeIfAbsent(String.valueOf(indexTemplateId).concat("_").concat(indexName), templateIdIndexName -> {
                        IndexNameAccessCountPO po = new IndexNameAccessCountPO();
                        po.setTemplateId(indexTemplateId).setLogicTemplteId(logicTemplateId).setClusterName(clusterName).setTemplateName(indexTemplateName).setDate(date);
                        return po;
                    });
                    indexNameAccessCountPO.increase(accessIndexCount);
                    indexNameAccessCountPO.setIndexName(indexName);

                    appIdTemplateAccessCountPO = appIdTemplateAccessCountMap.computeIfAbsent(String.valueOf(indexTemplateId).concat("_").concat(appId.toString()), templateIdAppId -> {
                        AppIdTemplateAccessCountPO po = new AppIdTemplateAccessCountPO();
                        po.setTemplateId(indexTemplateId).setLogicTemplateId(logicTemplateId).setClusterName(clusterName).setTemplateName(indexTemplateName).setDate(date);
                        po.setAccessDetailInfo(Maps.newHashMap());
                        return po;
                    });

                    appIdTemplateAccessCountPO.increase(accessIndexCount);
                    appIdTemplateAccessCountPO.setAppId(appId);


                    if (!appIdTemplateAccessCountPO.getAccessDetailInfo().containsKey(indexName)) {
                        appIdTemplateAccessCountPO.getAccessDetailInfo().put(indexName, 0L);
                    }
                    Long count = appIdTemplateAccessCountPO.getAccessDetailInfo().get(indexName);
                    count += accessIndexCount;

                    appIdTemplateAccessCountPO.getAccessDetailInfo().put(indexName, count);
                }
            } // end for indexName

        } // end for appId

        // 对没有访问的模板设置访问次数
        for (IndexTemplatePhyWithLogic item : indexTemplateList) {

            if (!templateAccessCountMap.containsKey(String.valueOf(item.getId()))) {
                TemplateAccessCountPO po = new TemplateAccessCountPO();
                po.setTemplateId(item.getId().intValue()).setLogicTemplateId(item.getLogicId()).setClusterName(item.getCluster()).setTemplateName(item.getName()).setDate(date);
                po.setCount(0L);
                LOGGER.error("class=QueryStatisticsCollector||method=statisticsAppIdAccessDetailInfo||errMsg=clusterName {} templateName -> {} not access",
                        item.getCluster(), item.getName());
                templateAccessCountMap.put(String.valueOf(item.getId()), po);
                ++noAccessTemplateCount;
                noAccessTemplateNames.add(item.getName());
            }
        }

        LOGGER.error("class=QueryStatisticsCollector||method=statisticsAppIdAccessDetailInfo||errMsg=noAccessTemplateCount {}||noAccessTemplateNames={}",
                noAccessTemplateCount, noAccessTemplateNames);

        return stResult;
    }

    /**
     * 采集任务子线程
     */
    class QueryStatisticsCollectorCallable implements Callable<Map<Integer/*appId*/, Map<String/*indexName*/, LongAdder/*access indexName count*/>>> {
        /**
         * 索引日期
         */
        private String indexDate;
        /**
         * shard编号
         */
        private Integer shardNo;


        public QueryStatisticsCollectorCallable(String indexDate,
                                                Integer shardNo) {
            this.indexDate = indexDate;
            this.shardNo = shardNo;
        }

        @Override
        public Map<Integer/*appId*/, Map<String/*indexName*/, LongAdder/*access indexName count*/>> call() throws Exception {
            Thread.currentThread().setName(String.format("gateway_join_%s_%d", indexDate, shardNo));

            Map<Integer/*appId*/, Map<String/*indexName*/, LongAdder/*access indexName count*/>> appIdAccessDetailMap = Maps.newLinkedHashMap();
            LongAdder count = new LongAdder();

            gatewayJoinEsDao.scrollRequestLogByShardNo(indexDate, shardNo, resultList -> {

                if (CollectionUtils.isEmpty(resultList)) {
                    LOGGER.error("class=QueryStatisticsCollectorCallable||method=call||errMsg=resultList is empty");
                    return;
                }
                count.add(resultList.size());

                Set<String/*indexName*/> accessIndexNameSet = null;
                Map<String/*indexName*/, LongAdder/*access indexName count*/> indexNameAccessMap = null;

                for (GatewayJoinPO item : resultList) {
                    if (item == null) {
                        continue;
                    }

                    if (item.getAppid() == null) {
                         LOGGER.error("class=QueryStatisticsCollectorCallable||method=call||errMsg=appid is null, requestId {}", item.getRequestId());
                        continue;
                    }

                    // 对一次查询的索引进行去重
                    accessIndexNameSet = removeDuplicateIndexName(item.getIndices());
                    if (accessIndexNameSet == null) {
                        LOGGER.error("class=QueryStatisticsCollectorCallable||method=call||indexName={}||requestId={}||errMsg=indexName is empty!",
                                item.getIndices(), item.getRequestId());
                        continue;
                    }

                    // 多type索引查询映射字段不为空
                    if (StringUtils.isNoneBlank(item.getDestIndexName())) {
                        Set<String> mulityTypeIndexNames = removeDuplicateIndexName(item.getDestIndexName());
                        if (CollectionUtils.isNotEmpty(mulityTypeIndexNames)) {
                            accessIndexNameSet.addAll(mulityTypeIndexNames);
                        }
                    }

                    // 进行appId维度的统计
                    indexNameAccessMap = appIdAccessDetailMap.computeIfAbsent(item.getAppid(), appId -> Maps.newLinkedHashMap());

                    // 对每个索引进行次数累加
                    for (String indexName : accessIndexNameSet) {
                        indexNameAccessMap.computeIfAbsent(indexName, key -> new LongAdder()).increment();
                    }

                }

            } );

            LOGGER.info("class=QueryStatisticsCollectorCallable||method=call||shardNo={}||count={}",
                    shardNo, count.longValue());

            totalCount.add(count.longValue());

            return appIdAccessDetailMap;
        }

        /**
         * 去除重复的索引
         *
         * @param indices
         * @return
         */
        private Set<String> removeDuplicateIndexName(String indices) {

            if (StringUtils.isBlank(indices)) {
                return new HashSet<>();
            }

            String[] indexNameArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(
                    StringUtils.removeEnd(indices, COMMA), COMMA);
            if (indexNameArr == null || indexNameArr.length <= 0) {
                return new HashSet<>();
            }

            Set<String> indexNameSets = Sets.newHashSet();
            for (String indexName : indexNameArr) {
                indexNameSets.add(indexName);
            }

            return indexNameSets;
        }
    }
}
