package com.didichuxing.datachannel.arius.admin.metadata.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.AppIdTemplateAccessCount;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.TemplateStatsInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.weekly.AppQuery;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslFieldUsePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.AppIdTemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.AppQueryPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.TemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.TemplateTpsMetricPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateStatsInfoPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.app.AppIdTemplateAccessESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslFieldUseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayJoinESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index.IndexHealthDegreeDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexNodeInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIngestInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateAccessESDAO;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.TypeDefine;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.TypeDefineOperator;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.didichuxing.datachannel.arius.admin.common.util.CommonUtils.formatDouble;

@Service
public class TemplateSattisService {

    protected static final ILog LOGGER = LogFactory.getLog(TemplateSattisService.class);

    @Autowired
    private AriusStatsIndexInfoESDAO        ariusStatsIndexInfoESDAO;

    @Autowired
    private AriusStatsIndexNodeInfoESDAO    ariusStatsIndexNodeInfoESDAO;

    @Autowired
    private AriusStatsIngestInfoESDAO       ariusStatsIngestInfoESDAO;

    @Autowired
    private IndexHealthDegreeDAO            indexHealthDegreeDAO;

    @Autowired
    private GatewayJoinESDAO                gatewayJoinESDAO;

    @Autowired
    private TemplateAccessESDAO             templateAccessESDAO;

    @Autowired
    private AppIdTemplateAccessESDAO        appIdTemplateAccessESDAO;

    @Autowired
    private DslFieldUseESDAO                dslFieldUseESDAO;

    @Autowired
    private TemplateValueService            templateValueService;

    @Autowired
    private TemplateLogicService            templateLogicService;

    @Autowired
    private ESTemplateService               esTemplateService;

    private static final Long               ONE_DAY    = 24 * 60 * 60 * 1000L;
    private static final Long               MINS_OF_15 = 15 * 60 * 1000L;
    private static final Long               ONE_GB     = 1024 * 1024 * 1024L;

    private static final String             INDEX_STR       = "index";
    private static final String             DOC_VALUES_STR  = "doc_values";
    private static final String             FALSE_STR       = "false";
    private static final String             NO_STR          = "no";

    /**
     * 根据模板名称和集群获取，模板所在节点【startTime， endTime】内的平均cpu
     * @param templateId
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Tuple<String/*node*/, Double/*cpuUsage*/>> getTemplateNodeCpu(Long templateId, Long startDate,
                                                                              Long endDate) {
        return ariusStatsIndexNodeInfoESDAO.getTemplateNodeCpu(templateId, startDate, endDate);
    }

    /**
     * 获取模板最近一段时间在访问的app列表
     *
     * @param logicTemplateId 模板id
     * @param days 结束日期
     * @return appid列表
     */
    
    public Result<Map<Integer, Long>> getTemplateAccessAppIds(Integer logicTemplateId, int days) {
        List<AppIdTemplateAccessCountPO> accessCountPos = appIdTemplateAccessESDAO.getAccessAppidsInfoByTemplateId(logicTemplateId, days);
        if(CollectionUtils.isEmpty(accessCountPos)){
            return Result.buildSucc();
        }

        Map<Integer, Long> ret = new HashMap<>();
        for(AppIdTemplateAccessCountPO accessCountPo : accessCountPos){
            Integer appid = accessCountPo.getAppId();
            Long    count = accessCountPo.getCount();

            if(null != ret.get(appid)){
                count += ret.get(appid);
            }
            ret.put(appid, count);
        }

        return Result.buildSucc(ret);
    }

    /**
     * 获取模板指标信息
     *
     * 使用场景：
     *  容量规划plan任务  每个模板一天一次
     *  容量规划check任务 每个模板15min一次
     *
     *  模板quota使用量统计任务  每个模板15min一次
     *
     *  用户扩缩容申请  每天100次以内
     *
     * @param cluster       集群
     * @param templateList  物理模板的名称
     * @param startDate     毫秒
     * @param endDate       毫秒
     * @return
     */
    
    public Result<List<TemplateMetric>> getTemplateMetrics(String cluster, List<String> templateList, long startDate,
                                                           long endDate) {
        Long start = System.currentTimeMillis();
        Long min30 = 30 * 60 * 1000L;

        List<TemplateMetric> templateMetrics = new CopyOnWriteArrayList<>();

        templateList.parallelStream().forEach(template -> {
            TemplateMetric templateMetric = new TemplateMetric();

            try {
                templateMetric.setCluster(cluster);
                templateMetric.setTemplate(template);

                FutureUtil.DEAULT_FUTURE.runnableTask(() -> {
                    double totalSizeInBytes = ariusStatsIndexInfoESDAO.getTemplateTotalSizeByTimeRange(template,
                            cluster, endDate - min30, endDate);
                    templateMetric.setSumIndexSizeG(totalSizeInBytes / ONE_GB);
                }).runnableTask(() -> {
                    long totalDocNu = ariusStatsIndexInfoESDAO.getTemplateTotalDocNuByTimeRange(template, cluster,
                            endDate - min30, endDate);
                    templateMetric.setSumDocCount(totalDocNu);
                }).runnableTask(() -> {
                    double maxIndexSize = ariusStatsIndexInfoESDAO.getTemplateMaxIndexSize(template, cluster,
                            endDate - min30, endDate);
                    templateMetric.setMaxIndexSizeG(maxIndexSize / ONE_GB);
                }).runnableTask(() -> {
                    long maxIndexDoc = ariusStatsIndexInfoESDAO.getTemplateMaxIndexDoc(template, cluster,
                            endDate - min30, endDate);
                    templateMetric.setMaxIndexDocCount(maxIndexDoc);
                }).runnableTask(() -> {
                    Map<String, String> maxInfo = ariusStatsIndexInfoESDAO.getTemplateMaxInfo(template, cluster,
                            startDate, endDate);
                    setMaxField(templateMetric, maxInfo);
                }).waitExecute();
            } catch (Exception e) {
                LOGGER.error("class=TemplateStatisController||method=getCapacityMetric||template={}||cluster={}",
                        template, cluster);
            }

            templateMetrics.add(templateMetric);

            if (!EnvUtil.isOnline()) {
                LOGGER.info(
                        "class=TemplateStatisController||method=getCapacityMetric||template={}||cluster={}||templateMetric={}||cost={}",
                        template, cluster, JSON.toJSONString(templateMetric), System.currentTimeMillis() - start);
            }
        });

        return Result.buildSucc(templateMetrics);
    }

    private void setMaxField(TemplateMetric templateMetric, Map<String, String> maxInfo) {
        if (StringUtils.isNotBlank(maxInfo.get("max_tps"))) {
            templateMetric.setMaxTps(Double.valueOf(maxInfo.get("max_tps")));
        } else {
            templateMetric.setMaxTps(0d);
        }
        if (StringUtils.isNotBlank(maxInfo.get("max_query_time"))) {
            templateMetric.setMaxQueryTime(Double.valueOf(maxInfo.get("max_query_time")));
        } else {
            templateMetric.setMaxQueryTime(0d);
        }
        if (StringUtils.isNotBlank(maxInfo.get("max_scroll_time"))) {
            templateMetric.setMaxScrollTime(Double.valueOf(maxInfo.get("max_scroll_time")));
        } else {
            templateMetric.setMaxScrollTime(0d);
        }
    }

    /**
     * 获取模板tps指标
     *
     * @param logicId 模板id
     * @param currentStartDate 开始时间
     * @param currentEndDate   结束时间
     * @return
     */
    
    public Result<LogicTemplateTpsMetric> getTemplateTpsMetric(Integer logicId, Long currentStartDate,
                                                               Long currentEndDate) {
        if (logicId == null){ return Result.build(ResultType.ILLEGAL_PARAMS);}

        // 如果时间值为空，不传则为最近15分钟
        if (currentStartDate == null || currentEndDate == null) {
            currentStartDate = System.currentTimeMillis() - MINS_OF_15;
            currentEndDate = System.currentTimeMillis();
        }

        TemplateTpsMetricPO tpsMetricPO = null;
        Map<Long/*templateId*/, Double> currentFailCountMap = null;
        try {
            Long startDate = System.currentTimeMillis();
            Long endDate   = startDate - 7 * ONE_DAY;
            tpsMetricPO = ariusStatsIndexInfoESDAO.getTemplateTpsMetricInfo(logicId, startDate, endDate,
                    currentStartDate, currentEndDate);

            currentFailCountMap = ariusStatsIngestInfoESDAO.getTemplateIngestFailMetricInfo(logicId,
                    System.currentTimeMillis() - MINS_OF_15, System.currentTimeMillis());

            if (!Objects.isNull(tpsMetricPO)) {
                tpsMetricPO.setCurrentFailCountMap(currentFailCountMap);
            }

            return Result.buildSucc( ConvertUtil.obj2Obj(tpsMetricPO, LogicTemplateTpsMetric.class));
        } catch (Exception e) {
            LOGGER.error(
                    "class=TemplateStatisController||method=getTemplateTpsMetricInfo||errMsg=fail to get {} TpsMetricInfo",
                    logicId, e);
            return Result.buildFail();
        }
    }

    /**
     * 获取指定索引的访问次数
     *
     * @param indexNames 索引  ,间隔
     * @param startDate  开始时间
     * @param endDate    结束时间
     * @return result
     */
    
    public Result<List<IndexNameQueryAvgRate>> getIndexAccessCount(String indexNames, Long startDate, Long endDate) {
        if (StringUtils.isBlank(indexNames) || startDate == null || endDate == null || endDate < startDate) {
            return Result.build(ResultType.ILLEGAL_PARAMS);
        }

        return Result.buildSucc(ConvertUtil.list2List(ariusStatsIndexInfoESDAO.getIndexNameQueryAvgRate(indexNames, startDate, endDate),
                IndexNameQueryAvgRate.class));
    }

    /**
     * 获取价值分
     * @return result
     */
    
    public Result<List<IndexTemplateValue>> listTemplateValue() {
        return Result.buildSucc(ConvertUtil.list2List(templateValueService.listAll(), IndexTemplateValue.class));
    }


    /**
     * 获取模板的价值分
     *
     * @param logicTemplateId
     * @return result
     */
    
    public Result<IndexTemplateValue> getTemplateValue(Integer logicTemplateId) {
        return Result.buildSucc(ConvertUtil.obj2Obj(templateValueService.getTemplateValueByLogicTemplateId(logicTemplateId), IndexTemplateValue.class));
    }

    /**
     * 获取mapping优化信息
     *
     * @param cluster  集群
     * @param template 模板名称
     * @return result
     */
    public Result<MappingOptimize> getMappingOptimize(String cluster, String template) {
        MappingConfig mappingConfig = esTemplateService.syncGetMappingsByClusterName(cluster, template);
        if (mappingConfig == null) {
            return Result.buildFail();
        }

        DslFieldUsePO dslFieldUsePo = dslFieldUseESDAO.getFieldUseSummeryInfoByTemplateName(template);
        Map<String, Long> where = dslFieldUsePo.getWhereFieldsCounter();
        Map<String, Long> group = dslFieldUsePo.getGroupByFieldsCounter();
        Map<String, Long> sort = dslFieldUsePo.getSortByFieldsCounter();

        MappingOptimize mappingOptimize = new MappingOptimize(cluster, template);

        Map<String, Map<String, TypeDefine>> typeMap = mappingConfig.getTypeDefines();

        for(Map.Entry<String, Map<String, TypeDefine>> entry : typeMap.entrySet()){
            String type = entry.getKey();
            for (String field : typeMap.get(type).keySet()) {
                TypeDefine typeDefine = typeMap.get(type).get(field);

                boolean index = false;
                boolean docValue = false;

                // sort/group
                if(sort.containsKey(field) || group.containsKey(field)){docValue = true;}

                // where
                if(where.containsKey(field)){index = true;}

                // 特殊类型不需要优化
                if(TypeDefineOperator.isNotOptimze(typeDefine.getDefine())){continue;}

                boolean optmize = false;
                JSONObject tgtTypeJson = new JSONObject( Maps.newHashMap(typeDefine.getDefine()));
                // 看是否能够优化关闭index
                if (!index &&
                    (!(FALSE_STR.equalsIgnoreCase(tgtTypeJson.getString(INDEX_STR))
                            || NO_STR.equalsIgnoreCase(tgtTypeJson.getString(INDEX_STR))))) {
                        tgtTypeJson.put(INDEX_STR, false);
                        optmize = true;
                }

                if (!docValue &&
                    (!(FALSE_STR.equalsIgnoreCase(tgtTypeJson.getString(DOC_VALUES_STR))))) {
                        tgtTypeJson.put(DOC_VALUES_STR, false);
                        optmize = true;
                }

                if (optmize) {
                    mappingOptimize.addOptimize(new MappingOptimizeItem(type, field, typeDefine.getDefine(), tgtTypeJson));
                }
            }
        }

        return Result.buildSucc(mappingOptimize);
    }

    /**
     * 获取模板的基本统计信息
     *
     * @param logicTemplateId 模版id
     * @param startDate
     * @param endDate
     * @return
     */
    
    public Result<IndexTemplateLogicWithStats> getTemplateBaseStatsInfo(Long logicTemplateId, Long startDate, Long endDate) {
        if (logicTemplateId == null) {
            return Result.build(ResultType.ILLEGAL_PARAMS);
        }

        IndexTemplateLogic indexTemplate = templateLogicService.getLogicTemplateById(logicTemplateId.intValue());
        if (null == indexTemplate) {
            return Result.buildFail();
        }

        IndexTemplateLogicWithStats indexTemplateLogicWithStats = (IndexTemplateLogicWithStats)indexTemplate;
        FutureUtil.DEAULT_FUTURE.runnableTask(() -> {
            double totalSizeInBytes = ariusStatsIndexInfoESDAO.getLogicTemplateTotalSize(logicTemplateId);
            indexTemplateLogicWithStats.setStore(formatDouble(totalSizeInBytes / ONE_GB, 2));
        }).runnableTask(() -> {
            double templateHealthDegree = indexHealthDegreeDAO.getTemplateAvgDegree(logicTemplateId, endDate - ONE_DAY,
                    endDate);
            indexTemplateLogicWithStats.setIndexHealthDegree(formatDouble(templateHealthDegree, 2));
        }).runnableTask(() -> {
            Integer templaterValueDegree = templateValueService
                    .getTemplateValueByLogicTemplateId(logicTemplateId.intValue()).getValue();
            indexTemplateLogicWithStats.setIndexValueDegree(formatDouble(templaterValueDegree, 2));
        }).runnableTask(() -> {
            Double templateQpsAvgInfo = ariusStatsIndexInfoESDAO.getTemplateQpsAvgInfo(logicTemplateId, startDate,
                    endDate);
            indexTemplateLogicWithStats.setAvgQps(formatDouble(templateQpsAvgInfo, 2));
        }).runnableTask(() -> {
            Double templateTpsAvgInfo = ariusStatsIndexInfoESDAO.getTemplateTpsAvgInfo(logicTemplateId, startDate,
                    endDate);
            indexTemplateLogicWithStats.setAvgTps(formatDouble(templateTpsAvgInfo, 2));
        }).waitExecute();

        return Result.buildSucc(indexTemplateLogicWithStats);
    }

    /**
     * 获取appID查询信息(top10)
     *
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    
    public Result<List<AppQuery>> getQueryTopNumInfo(Integer appId, Long startDate, Long endDate) {
        if (null == appId || null == startDate || null == endDate) {
            return Result.build(ResultType.ILLEGAL_PARAMS);
        }
        List<AppQueryPO> queryInfoList = gatewayJoinESDAO.getQueryTopNumInfoByAppid(appId, startDate,
                endDate, 10);
        if (CollectionUtils.isEmpty(queryInfoList)) {
            return Result.build(ResultType.NOT_EXIST);
        }

        return Result.buildSucc(ConvertUtil.list2List(queryInfoList, AppQuery.class));
    }

    public Result<Map<Integer, Long>> getAccessStatsInfoByTemplateIdAndDays(int logicTemplateId, int days) {
        List<AppIdTemplateAccessCountPO> accessCountPos = appIdTemplateAccessESDAO.getAccessAppidsInfoByTemplateId(logicTemplateId, days);
        if(CollectionUtils.isEmpty(accessCountPos)){
            Result.build(ResultType.SUCCESS);
        }

        Map<Integer, Long> ret = new HashMap<>();
        for(AppIdTemplateAccessCountPO accessCountPo : accessCountPos){
            Integer appid = accessCountPo.getAppId();
            Long    count = accessCountPo.getCount();

            if(null != ret.get(appid)){
                count += ret.get(appid);
            }
            ret.put(appid, count);
        }

        return Result.buildSucc(ret);
    }

    public Result<TemplateStatsInfo> getTemplateBaseStatisticalInfoByLogicTemplateId(Long logicTemplateId) {
        long current = System.currentTimeMillis();

        IndexTemplateLogic indexTemplate = templateLogicService.getLogicTemplateById(logicTemplateId.intValue());

        if (null == indexTemplate) {
            return Result.buildFail("无法找到对应的模板");
        }

        TemplateStatsInfoPO templateStatsInfoPO = new TemplateStatsInfoPO();
        templateStatsInfoPO.setTemplateId(logicTemplateId);
        templateStatsInfoPO.setQutoa(indexTemplate.getQuota());

        FutureUtil.DEAULT_FUTURE.runnableTask(() -> {
            double indexHealthDegree = indexHealthDegreeDAO.getTemplateAvgDegree(logicTemplateId, current - ONE_DAY,
                    current);
            templateStatsInfoPO.setIndexHealthDegree(formatDouble(indexHealthDegree, 2));
        }).runnableTask(() -> {
            double totalSizeInBytes = ariusStatsIndexInfoESDAO.getLogicTemplateTotalSize(logicTemplateId);
            templateStatsInfoPO.setStoreBytes(totalSizeInBytes);
            templateStatsInfoPO.setStore(formatDouble(totalSizeInBytes / ONE_GB, 2));
        }).runnableTask(() -> {
            double maxTps = ariusStatsIndexInfoESDAO.getTemplateMaxTpsByTimeRange(logicTemplateId, current - ONE_DAY,
                    current);
            templateStatsInfoPO.setWriteTps(formatDouble(maxTps, 2));
        }).runnableTask(() -> {
            long docNu = ariusStatsIndexInfoESDAO.getTemplateTotalDocNuByTimeRange(logicTemplateId, current - ONE_DAY,
                    current);
            templateStatsInfoPO.setDocNu(docNu);
        }).runnableTask(() -> {
            String indexName = indexTemplate.getName();
            List<String> topics = Lists.newArrayList();
            templateStatsInfoPO.setTopics(topics);
            templateStatsInfoPO.setTemplateName(indexName);
        }).runnableTask(() -> {
            List<TemplateAccessCountPO> templateAccessCountPos = templateAccessESDAO
                    .getTemplateAccessLastNDayByLogicTemplateId(logicTemplateId.intValue(), 7);
            if (CollectionUtils.isNotEmpty(templateAccessCountPos)) {
                Long count = 0L;
                for (TemplateAccessCountPO po : templateAccessCountPos) {
                    count += po.getCount();
                }

                templateStatsInfoPO.setAccessCountPreDay((double)count / templateAccessCountPos.size());
            }
        }).waitExecute();

        return Result.buildSucc(ConvertUtil.obj2Obj(templateStatsInfoPO, TemplateStatsInfo.class));
    }

    public Result<List<AppIdTemplateAccessCount>> getAccessAppInfos(int logicTemplateId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
                appIdTemplateAccessESDAO.getAccessAppidsInfoByTemplateId(logicTemplateId, startDate, endDate),
                AppIdTemplateAccessCount.class));
    }

    public Result<List<ESIndexStats>> getIndexStatis(Long logicTemplateId, Long startDate, Long endDate) {
        return Result.buildSucc(ariusStatsIndexInfoESDAO.getTemplateRealStatis(logicTemplateId, startDate - 1 * 60 * 1000L, endDate));
    }
}
