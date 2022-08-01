package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl;

import static com.didichuxing.datachannel.arius.admin.common.RetryUtils.performTryTimesMethods;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslBase;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslQueryLimit;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.util.DSLSearchUtils;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class DslTemplateESDAO extends BaseESDAO {

    /**
     * 查询模板数据的索引名称
     */
    private String           indexName;
    /**
     * 查询模板过期删除时间
     */
    @Value("${delete.expired.template.time}")
    private String           deleteExpireDslTime;
    /**
     * 查询历史数据时间
     */
    @Value("${history.query.time}")
    private String           historyQueryTime;
    /**
     * type名称
     */
    private String           typeName    = "type";
    /**
     * 滚动查询大小
     */
    private static final int SCROLL_SIZE = 1000;

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusDslTemplate();
    }

    /**
     * 更新查询模板信息
     *
     * @param list
     * @return
     */
    public boolean updateTemplates(List<DslTemplatePO> list) {

        return updateClient.batchUpdate(indexName, typeName, list);
    }

    /**
     * 获取所有查询模板最近修改时间在(now-1d,now)范围内，并且不启用的查询模板，然后删除过期的查询模板数据
     *
     * @return
     */
    public boolean deleteExpiredDslTemplate() {

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_EXPIRED_DSL_TEMPLATE, SCROLL_SIZE,
            deleteExpireDslTime);

        List<DslTemplatePO> list = getTemplatesByDsl(dsl);
        List<String> ids = Lists.newLinkedList();
        for (DslTemplatePO dslTemplate : list) {
            ids.add(dslTemplate.getKey());
        }

        return updateClient.batchDelete(indexName, typeName, ids);
    }

    /**
     * 更新查询限流值
     *
     * @param dslQueryLimitList
     * @return
     */
    public boolean updateQueryLimitByProjectIdDslTemplate(List<DslQueryLimit> dslQueryLimitList) {
        String ariusModifyTime = DateTimeUtil.getCurrentFormatDateTime();
        List<DslTemplatePO> list = Lists.newLinkedList();
        DslTemplatePO item = null;

        for (DslQueryLimit dslQueryLimit : dslQueryLimitList) {
            item = new DslTemplatePO();
            item.setQueryLimit(dslQueryLimit.getQueryLimit());
            // 强制设置查询限流值设置为 true
            item.setForceSetQueryLimit(true);
            item.setAriusModifyTime(ariusModifyTime);
            item.setProjectId(dslQueryLimit.getProjectId());
            item.setDslTemplateMd5(dslQueryLimit.getDslTemplateMd5());

            list.add(item);
        }

        return updateTemplates(list);
    }

    /**
     * 获取查询模板信息
     *
     * @param dslBases
     * @return
     */
    public Map<String, DslTemplatePO> getDslTemplateByKeys(List<? extends DslBase> dslBases) {
        Map<String, DslTemplatePO> result = Maps.newHashMap();
        for (DslBase dslBase : dslBases) {
            result.put(dslBase.getProjectIdDslTemplateMd5(),
                getDslTemplateByKey(dslBase.getProjectId(), dslBase.getDslTemplateMd5()));
        }
        return result;
    }

    /**
     * 根据主键id获取查询模板
     *
     * @param projectId
     * @param dslTemplateMd5
     * @return
     */
    public DslTemplatePO getDslTemplateByKey(Integer projectId, String dslTemplateMd5) {
        return getDslTemplateByKey(String.format("%d_%s", projectId, dslTemplateMd5));
    }

    /**
     * 根据主键id获取查询模板
     *
     * @param key
     * @return
     */
    public DslTemplatePO getDslTemplateByKey(String key) {
        return gatewayClient.doGet(indexName, typeName, key, DslTemplatePO.class);
    }

    /**
     * 获取某个project id的所有查询模板数据,已排除老查询模板
     *
     * @param projectId
     * @return
     */
    public List<DslTemplatePO> getAllDslTemplateByProjectId(Integer projectId) {

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_TEMPLATE_BY_PROJECT_ID, SCROLL_SIZE,
            projectId);

        return getTemplatesByDsl(dsl);
    }

    /**
     * 获取查询模板创建时间大于指定天偏移的查询模板数据，用于设置慢查耗时阈值
     *
     * @param dayOffset
     * @return
     */
    public List<DslTemplatePO> getDslTemplatesByDateRange(int dayOffset) {

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_TEMPLATES_BY_RANGE, SCROLL_SIZE,
            dayOffset);

        return getTemplatesByDsl(dsl);
    }

    /**
     * 获取早期查询模板，不包括手动修改限流值
     *
     * @return
     */
    public List<DslTemplatePO> getEarliestDslTemplate() {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_EARLIEST_DSL_TEMPLATES, SCROLL_SIZE);

        return getTemplatesByDsl(dsl);
    }

    /**
     * 获取一段时间不使用的查询模板，不包括黑名单和手动修改限流值
     *
     * @return
     */
    public List<DslTemplatePO> getLongTimeNotUseDslTemplate() {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_LONG_TIME_NOT_USE_DSL_TEMPLATES, SCROLL_SIZE,
            historyQueryTime);

        return getTemplatesByDsl(dsl);
    }

    public List<DslTemplatePO> getDslMetricsByProjectId(Integer projectId, Long startDate, Long endDate) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_TEMPLATE_BY_PROJECT_ID_AND_RANGE, 10000,
            startDate, endDate, projectId);

        return gatewayClient.performRequest(indexName, typeName, dsl, DslTemplatePO.class);
    }

    private String buildQueryCriteriaDsl(Integer projectId, String dslTemplateMd5, String queryIndex, Long startTime,
                                         Long endTime) {
        return "[" + buildQueryCriteriaCell(projectId, dslTemplateMd5, queryIndex, startTime, endTime) + "]";
    }

    private String buildQueryCriteriaCell(Integer projectId, String dslTemplateMd5, String queryIndex, Long startTime,
                                          Long endTime) {
        List<String> cellList = Lists.newArrayList();

        // 最近时间范围条件
        cellList.add(DSLSearchUtils.getTermCellForRangeSearch(startTime, endTime, "timeStamp"));
        // projectId 条件
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(projectId, "projectId"));

        if (StringUtils.isNotBlank(dslTemplateMd5)) {
            // 优先使用 dslTemplateMd5 条件
            cellList.add(DSLSearchUtils.getTermCellForExactSearch(dslTemplateMd5, "dslTemplateMd5"));
        } else if (StringUtils.isNotBlank(queryIndex)) {
            // queryIndex 条件
            cellList.add(DSLSearchUtils.getTermCellForPrefixSearch(queryIndex, "indices"));
        }
        return ListUtils.strList2String(cellList);
    }

    /**
     * 根据查询条件分页获取DSL模板数据
     *
     * @param projectId    应用id
     * @param queryDTO 查询条件
     */
    public Tuple<Long, List<DslTemplatePO>> getDslTemplatePage(Integer projectId, DslTemplateConditionDTO queryDTO) {
        String queryCriteriaDsl = buildQueryCriteriaDsl(projectId, queryDTO.getDslTemplateMd5(),
            queryDTO.getQueryIndex(), queryDTO.getStartTime(), queryDTO.getEndTime());

        // 排序条件，默认根据使用时间排序 desc
        String sortInfo = "timeStamp";
        String sortOrder = "desc";
        if (!StringUtils.isEmpty(queryDTO.getSortInfo())) {
            // 根据用户自定义条件排序
            sortOrder = BooleanUtils.isTrue(queryDTO.getOrderByDesc()) ? "desc" : "asc";
            sortInfo = queryDTO.getSortInfo();
        }

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_TEMPLATE_BY_CONDITION,
            (queryDTO.getPage() - 1) * queryDTO.getSize(), queryDTO.getSize(), queryCriteriaDsl, sortInfo, sortOrder);

        return performTryTimesMethods(()->gatewayClient.performRequestListAndGetTotalCount(null, indexName, typeName,
                dsl, DslTemplatePO.class), Objects::isNull,3);
    }

    /**
     * 根据查询条件获取查询模板数据
     *
     * @param projectId
     * @param startDate
     * @param endDate
     * @return
     */
    public Tuple<Long, List<DslTemplatePO>> getDslTemplateByCondition(Integer projectId, String searchKeyword,
                                                                      String dslTag, String sortInfo, Long from,
                                                                      Long size, Long startDate, Long endDate) {
        try {
            String dsl = null;
            JSONArray mustJson = new JSONArray();
            mustJson.add(JSON.parse("{\"term\":{\"version\":{\"value\":\"V2\"}}}"));
            // 根据最近使用的时间（timeStamp字段）来筛选
            mustJson.add(
                JSON.parse(String.format("{\"range\":{\"timeStamp\":{\"gte\":%d,\"lte\":%d}}}", startDate, endDate)));

            if (projectId != null) {
                mustJson.add(JSON.parse(String.format("{\"term\":{\"projectId\":{\"value\":%d}}}", projectId)));
            }
            if (StringUtils.isNoneBlank(searchKeyword)) {
                mustJson.add(JSON.parse(String.format("{\"wildcard\":{\"my_all_fields\":\"%s\"}}", searchKeyword)));
            }
            if (StringUtils.isNoneBlank(dslTag)) {
                String[] items = StringUtils.splitByWholeSeparatorPreserveAllTokens(dslTag, ",");
                JSONArray dslTagJson = new JSONArray();
                Arrays.asList(items).forEach(dslTagJson::add);
                String terms = String.format("{\"terms\":{\"dslTag\":%s}}", dslTagJson.toJSONString());
                mustJson.add(JSON.parse(terms));
            }
            if (StringUtils.isBlank(sortInfo)) {
                // 默认根据使用时间排序 desc
                sortInfo = "\"timeStamp\": {\"order\": \"desc\"}";
            }

            // 存在深分页问题, 解决方案scroll（官方不推荐）/search after（官方推荐使用）/业务上限制 1000后的分页
            dsl = String.format("{\"from\":%d,\"size\":%d,\"query\":{\"bool\":{\"must\":[%s]}},\"sort\":{%s}}", from,
                size, mustJson.toJSONString(), sortInfo);

            return gatewayClient.performRequestListAndGetTotalCount(null, indexName, typeName, dsl,
                DslTemplatePO.class);

        } catch (Exception e) {
            LOGGER.error("class=DslTemplateEsDao||method=getDslTemplateByCondition||errMsg=search template error", e);
            return null;
        }
    }

    /**
     * 获取过期的查询模板信息
     *
     * @return
     */
    public List<DslTemplatePO> getExpiredAndWillDeleteDslTemplate() {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_EXPIRED_DELETED_DSL_TEMPLATE, SCROLL_SIZE,
            deleteExpireDslTime);

        return getTemplatesByDsl(dsl);
    }

    /**
     * 获取最近查询模板，不包括手动修改限流值和黑名单
     *
     * @return
     */
    public List<DslTemplatePO> getNearestDslTemplate() {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_NEAREST_DSL_TEMPLATES, SCROLL_SIZE);

        return getTemplatesByDsl(dsl);
    }

    /**
     * 获取最近没有设置黑白名单的查询模板,已排除老查询模板
     *
     * @return
     */
    public List<DslTemplatePO> getNearestDslTemplateAccessable() {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_NEAREST_DSL_TEMPLATE_ACCESSABLE,
            SCROLL_SIZE);

        return getTemplatesByDsl(dsl);
    }

    /**
     * 获取到缺少ariusCreateTime字段的文档
     *
     * @return
     */
    public List<DslTemplatePO> getMissingAriusCreateTme() {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_MISSING_ARIUS_CREATE_TIME, SCROLL_SIZE);

        return getTemplatesByDsl(dsl);
    }

    /**
     * 获取某个projectId的查询模板个数,已排除老版本查询模板
     *
     * @param projectId
     * @return
     */
    public Long getTemplateCountByProjectId(Integer projectId) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_TEMPLATE_COUNT_BY_PROJECT_ID, projectId);

        return gatewayClient.performRequestAndGetTotalCount(indexName, typeName, dsl);
    }

    /**
     * 获取某个projectId的新增查询模板个数,已排除老查询模板
     *
     * @param projectId
     * @param date
     * @param today
     * @return
     */
    public Long getIncreaseTemplateCountByProjectId(Integer projectId, String date, String today) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_INCREASE_DSL_TEMPLATE_BY_PROJECT_ID,
            projectId, date, today);

        return gatewayClient.performRequestAndGetTotalCount(indexName, typeName, dsl);
    }

    /**
     * 根据index获得对应的templateMD5
     *
     * @param indexName
     * @return
     */
    public Map<String/*dslMd5*/, Set<String>/*dsls*/> getTemplateMD5ByIndexName(String indexName, Integer dayOffset) {

        Map<String/*dslMd5*/, Set<String>/*dsls*/> dslMap = Maps.newHashMap();
        String dsl = null;
        if (dayOffset == -1) {
            dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_TEMPLATES_BY_INDEXNAME, SCROLL_SIZE,
                indexName);
        } else {
            dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_TEMPLATES_BY_INDEXNAME_WITH_DAY_RANGE,
                SCROLL_SIZE, indexName, dayOffset);
        }

        gatewayClient.queryWithScroll(this.indexName, typeName, dsl, SCROLL_SIZE, null, DslTemplatePO.class,
            resultList -> {
                if (resultList != null) {
                    for (DslTemplatePO dslTemplate : resultList) {
                        dslMap.computeIfAbsent(dslTemplate.getDslTemplateMd5(), key -> Sets.newLinkedHashSet())
                            .add(dslTemplate.getDsl());
                    }
                }
            });

        return dslMap;
    }

    /**
     * 根据index获得对应的templateMD5
     *
     * @param indexName
     * @return
     */
    public Map<String/*dskMd5*/, Set<String>/*dsls*/> getTemplateMD5ByIndexNameAndProjectId(String indexName,
                                                                                            String projectId,
                                                                                            Integer dayOffset) {

        Map<String/*dskMd5*/, Set<String>/*dsls*/> dslMap = Maps.newHashMap();

        String dsl = null;
        if (dayOffset == -1) {
            dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_TEMPLATES_BY_INDEXNAME_PROJECT_ID,
                SCROLL_SIZE, indexName, projectId);
        } else {
            dsl = dslLoaderUtil.getFormatDslByFileName(
                DslsConstant.GET_DSL_TEMPLATES_BY_INDEXNAME_PROJECT_ID_WITH_DAY_RANGE, SCROLL_SIZE, indexName,
                projectId, dayOffset);
        }

        gatewayClient.queryWithScroll(this.indexName, typeName, dsl, SCROLL_SIZE, null, DslTemplatePO.class,
            resultList -> {
                if (resultList != null) {
                    for (DslTemplatePO dslTemplate : resultList) {
                        dslMap.computeIfAbsent(dslTemplate.getDslTemplateMd5(), key -> Sets.newLinkedHashSet())
                            .add(dslTemplate.getDsl());
                    }
                }
            });

        return dslMap;
    }

    /**
     * 滚动获取查询模板
     *
     * @param request
     * @return
     */
    @Nullable
    public ScrollDslTemplateResponse handleScrollDslTemplates(ScrollDslTemplateRequest request) {

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_DSL_TEMPLATES, request.getScrollSize(),
            request.getLastModifyTime(), request.getDslTemplateVersion());

        List<DslTemplatePO> list = Lists.newLinkedList();
        ESQueryResponse response = null;
        // 没有游标id，则开始滚动查询
        if (StringUtils.isBlank(request.getScrollId())) {

            response = gatewayClient.prepareScrollQuery(indexName, typeName, dsl, null, DslTemplatePO.class,
                resultList -> {
                    if (resultList != null) {
                        list.addAll(resultList);
                    }
                });

        } else {
            response = gatewayClient.queryScrollQuery(indexName, request.getScrollId(), DslTemplatePO.class,
                resultList -> {
                    if (resultList != null) {
                        list.addAll(resultList);
                    }
                });
        }

        if (response == null) {
            return null;
        }

        String scrollId = response.getUnusedMap().get("_scroll_id").toString();

        return ScrollDslTemplateResponse.builder().dslTemplatePoList(list).scrollId(scrollId).build();
    }

    /**
     * 根据查询语句获取查询模板数据
     *
     * @param dsl
     * @return
     */
    private List<DslTemplatePO> getTemplatesByDsl(String dsl) {
        List<DslTemplatePO> list = Lists.newLinkedList();

        gatewayClient.queryWithScroll(indexName, typeName, dsl, SCROLL_SIZE, null, DslTemplatePO.class, resultList -> {
            if (resultList != null) {
                list.addAll(resultList);
            }
        });

        return list;
    }
}