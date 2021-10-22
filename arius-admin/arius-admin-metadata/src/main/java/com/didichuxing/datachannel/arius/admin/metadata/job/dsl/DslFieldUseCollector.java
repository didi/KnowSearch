package com.didichuxing.datachannel.arius.admin.metadata.job.dsl;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslSearchFieldNameMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslFieldUsePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayJoinPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateFieldPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslFieldUseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayJoinESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateFieldESDAO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

@Component
public class DslFieldUseCollector extends AbstractMetaDataJob {

    /**
     * 近7天创建的
     */
    private final static int DAYS_7 = 7;
    /**
     * 操作dsl.field.use 索引
     */
    @Autowired
    private DslFieldUseESDAO dslFieldUseEsDao;
    /**
     * 操作gateway join 索引
     */
    @Autowired
    private GatewayJoinESDAO gatewayJoinEsDao;
    /**
     * 操作template.field 索引
     */
    @Autowired
    private TemplateFieldESDAO templateFieldEsDao;

    @Autowired
    private TemplatePhyService templatePhyService;


    @Override
    public Object handleJobTask(String params) {
        String date = params;
        LOGGER.info("class=DslFieldUseCollector||method=handleJobTask||params={}", params);

        if (StringUtils.isBlank(date)) {
            date = DateTimeUtil.getFormatDayByOffset(1);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("get search field");

        // key为索引名称，value为查询字段统计值
        Map<String, DslSearchFieldNameMetric> indexFieldNameUseMap = runTaskAndGetResult(date);

        stopWatch.stop().start("agg template use field");
        // key为模板名称，value为查询字段统计值
        Map<String, DslSearchFieldNameMetric> indexTemplateFieldUseMap = aggByIndexTemplateFieldUseMap(indexFieldNameUseMap);

        stopWatch.stop().start("analyze use field");
        List<DslFieldUsePO> DslFieldUsePOList = analyzeThenBuildDslFieldUseList(date, indexTemplateFieldUseMap);

        for(DslFieldUsePO po : DslFieldUsePOList) {
            po.removeBlankField();
        }

        stopWatch.stop().start("save result");
        boolean operatorResult = dslFieldUseEsDao.bathInsert(DslFieldUsePOList);

        LOGGER.info("class=DslFieldUseCollector||method=handleJobTask||msg=dslFieldUse size={}||cost={}||operatorResult={}",
                DslFieldUsePOList.size(), stopWatch.stop(), operatorResult);

        return JOB_SUCCESS;
    }

    /**
     * 根据索引找到对应索引模板，以索引模板维度统计
     *
     * @param indexFieldNameUseMap
     * @return
     */
    private Map<String, DslSearchFieldNameMetric> aggByIndexTemplateFieldUseMap(Map<String, DslSearchFieldNameMetric> indexFieldNameUseMap) {
        // 找到索引对应的索引模板
        Map<String, DslSearchFieldNameMetric> indexTemplateFieldUseMap = Maps.newHashMap();

        // 获取所有查询模板, key 索引模板名称
        List<IndexTemplatePhyWithLogic> indexTemplateList = templatePhyService.listTemplateWithLogic();

        // 缓存已经匹配过的索引对应的索引模板名
        Map<String/*indexName*/, Set<String/*match template name*/>> matchIndexTemplateCacheMap = Maps.newHashMap();
        Set<String> matchIndexTemplateNameSet;

        for (Map.Entry<String, DslSearchFieldNameMetric> entry : indexFieldNameUseMap.entrySet()) {
            String indexName = entry.getKey();

            if (matchIndexTemplateCacheMap.containsKey(indexName)) {
                matchIndexTemplateNameSet = matchIndexTemplateCacheMap.get(indexName);
            } else {
                // 根据索引名称找到对应索引模板
                matchIndexTemplateNameSet = IndexNameUtils.matchIndexTemplateBySearchIndexName(indexName, indexTemplateList);
                matchIndexTemplateCacheMap.put(indexName, matchIndexTemplateNameSet);

                if (matchIndexTemplateNameSet.isEmpty()) {
                    LOGGER.error("class=DslFieldUseCollector||method=aggByIndexTemplateFieldUseMap||errMsg=indexName -> [{}] not match any indexTemplate or alias", indexName);
                    continue;

                } else if (matchIndexTemplateNameSet.size() > 1) {
                    LOGGER.error("class=DslFieldUseCollector||method=aggByIndexTemplateFieldUseMap||errMsg=indexName -> [{}] match {} indexTemplate or alias, {}",
                            indexName, matchIndexTemplateNameSet.size(), matchIndexTemplateNameSet);
                }
            }

            if (matchIndexTemplateNameSet.isEmpty()) {
                continue;
            }

            for (String indexTemplateName : matchIndexTemplateNameSet) {
                DslSearchFieldNameMetric searchFieldNameMetric = indexTemplateFieldUseMap.computeIfAbsent(indexTemplateName, k -> new DslSearchFieldNameMetric());
                searchFieldNameMetric.mergeSearchFieldNameMetric(entry.getValue());
            }
        }

        return indexTemplateFieldUseMap;
    }

    /**
     * 分析并构建查询使用的字段
     *
     * @param date
     * @param indexTemplateFieldUseMap
     * @return
     */
    private List<DslFieldUsePO> analyzeThenBuildDslFieldUseList(String date, Map<String, DslSearchFieldNameMetric> indexTemplateFieldUseMap) {
        List<DslFieldUsePO> DslFieldUsePOList = Lists.newArrayList();
        // 从arius.template.field获取索引模板中所有字段数据
        List<TemplateFieldPO> TemplateFieldPOList = templateFieldEsDao.getAllTemplateFields();

        Set<String> allFieldsSet = null;
        DslFieldUsePO DslFieldUsePO = null;
        // 遍历所有模板
        for (TemplateFieldPO TemplateFieldPO : TemplateFieldPOList) {

            allFieldsSet = TemplateFieldPO.getKeySet();

            DslFieldUsePO = new DslFieldUsePO();
            DslFieldUsePO.setId((long)TemplateFieldPO.getId());
            DslFieldUsePO.setName(TemplateFieldPO.getName());
            DslFieldUsePO.setClusterName(TemplateFieldPO.getClusterName());
            DslFieldUsePO.setDate(date);
            DslFieldUsePO.setFieldCount((long)allFieldsSet.size());

            DslSearchFieldNameMetric searchFieldNameMetric = indexTemplateFieldUseMap.get(TemplateFieldPO.getName());
            if (searchFieldNameMetric != null) {
                Set<String> useFieldsSet = searchFieldNameMetric.getUseFieldSet();

                if (!useFieldsSet.isEmpty()) {
                    removeFieldSetItem(allFieldsSet, useFieldsSet);
                }

                DslFieldUsePO.updateFromSearchFieldMetric(searchFieldNameMetric);
            } else {
                LOGGER.error("class=DslFieldUseCollector||method=analyzeThenBuildDslFieldUseList||errMsg=can't find template {} search info", TemplateFieldPO.getName());
                DslFieldUsePO.fillEmptyMap();
            }

            // 得到最近创建的字段
            Set<String> recentFieldList = Sets.newHashSet();
            for (Map.Entry<String, String> entry : TemplateFieldPO.getTemplateFieldMap().entrySet()) {
                if (!DateTimeUtil.isBeforeDateTime(entry.getValue(), DAYS_7)) {
                    recentFieldList.add(entry.getKey());
                }
            }
            DslFieldUsePO.setRecentCreateFields(Lists.newArrayList(recentFieldList));

            // 没有使用的字段集合,需要排除最近创建的字段
            if (!recentFieldList.isEmpty()) {
                removeFieldSetItem(allFieldsSet, recentFieldList);
            }
            DslFieldUsePO.setNotUseFields(Lists.newArrayList(allFieldsSet));

            DslFieldUsePOList.add(DslFieldUsePO);
        }

        return DslFieldUsePOList;
    }

    /**
     * 运行任务获取结果
     *
     * @param date
     * @return
     */
    private Map<String/* indexName*/, DslSearchFieldNameMetric> runTaskAndGetResult(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (StringUtils.isBlank(date)) {
            date = dateTimeFormatter.format( ZonedDateTime.now().minus(1, ChronoUnit.DAYS));
        }
        LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        long startDateMils = instant.toEpochMilli();

        // 按小时进行任务切分
        final String queryIndexDate = date;

        // 合并一天内访问同一个索引有哪些查询模板
        Map<String/* indexName*/, Set<String> /*md5s*/> accessIndicesDslMd5Maps = Maps.newHashMap();
        int taskCount = 24 * 60;
        for (int i = 0; i < taskCount; ++i) {
            //  单次查询范围1分钟
            long start = startDateMils + i * (1 * 60 * 1000);
            long end = start + 1 * 60 * 1000;

            Map<String/* indexName*/, Set<String> /*md5s*/> maps = gatewayJoinEsDao.aggIndicesDslMd5ByRange(queryIndexDate, start, end);
            for (Map.Entry<String, Set<String>> entry : maps.entrySet()) {
                // 过滤掉含有[的索引名称
                if (entry.getKey().contains("[")) {
                    LOGGER.warn("class=DslFieldUseCollector||method=runTaskAndGetResult||msg=skip illegal index name {}", entry.getKey());
                    continue;
                }
                accessIndicesDslMd5Maps.computeIfAbsent(entry.getKey(), k -> Sets.newHashSet()).addAll(entry.getValue());
            }
        }

        // 合并查询结果
        Map<String/* indexName*/, DslSearchFieldNameMetric> allSearchFieldNameMetricMap = Maps.newHashMap();

        // 获取到每个索引查询模板使用的字段信息
        String indices = "";
        Tuple<Long, GatewayJoinPO> tuple = null;
        String[] indexNameArray = null;

        for (Map.Entry<String/* indexName*/, Set<String>/*md5s*/> entry : accessIndicesDslMd5Maps.entrySet()) {
            indices = entry.getKey();

            for (String md5 : entry.getValue()) {
                tuple = gatewayJoinEsDao.getPoByIndicesAndMd5(queryIndexDate, indices, md5);
                if (tuple == null) {
                    LOGGER.error("class=DslFieldUseCollector||method=runTaskAndGetResult||indexName={}||errMsg=can't find {}",
                            indices, md5);

                    continue;
                }

                // 重置GatewayJoinPO中字段统计次数
                tuple.v2().resetFieldUseCount(tuple.v1());

                indexNameArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(indices, AdminConstant.COMMA);
                for (String indexName : indexNameArray) {
                    DslSearchFieldNameMetric searchFieldNameMetric = allSearchFieldNameMetricMap.computeIfAbsent(indexName, k -> new DslSearchFieldNameMetric(indexName));
                    searchFieldNameMetric.mergeSearchFieldNameMetric(tuple.v2());
                }
            }
        }

        LOGGER.info("class=DslFieldUseCollector||method=runTaskAndGetResult||msg=allSearchFieldNameMetricMap size {}", allSearchFieldNameMetricMap.size());
        return allSearchFieldNameMetricMap;
    }


    /**
     * 移除已使用的字段
     *
     * @param allFieldsSet
     * @param useFieldsSet
     */
    private void removeFieldSetItem(Set<String> allFieldsSet, Set<String> useFieldsSet) {
        Iterator<String> iterator = allFieldsSet.iterator();
        String fieldName = "";

        while (iterator.hasNext()) {
            fieldName = iterator.next();
            if (useFieldsSet.contains(fieldName)) {
                iterator.remove();
            }
        }
    }
}
