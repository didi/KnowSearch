package com.didichuxing.datachannel.arius.admin.metadata.job.dsl;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslBase;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslAnalyzeResultQpsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslMetricsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslAnalyzeResultQpsESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslMetricsESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslTemplateESDAO;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

/**
 * @author cjm
 * 用于计算并设置(now-1d, now-30d)生成的DSL查询模版的限流值大小
 *
 */
@NoArgsConstructor
@Component
public class DslTemplateUpdateEarliestQueryLimitJob extends AbstractMetaDataJob {

    @Autowired
    private DslTemplateESDAO dslTemplateESDAO;

    @Autowired
    private DslMetricsESDAO dslMetricsESDAO;

    @Autowired
    private DslAnalyzeResultQpsESDAO dslAnalyzeResultQpsESDAO;

    /**
     * 默认的查询限流值
     */
    @Value("${default.query.limit}")
    private Integer defaultQueryLimit;

    /**
     * 查询限流因子
     */
    private static final double FACTOR = 2.0f;

    /**
     * 最小查询限流值
     */
    private static final Integer MIN_QUERY_LIMIT = 50;

    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=DslTemplateUpdateEarliestQueryLimitJob||method=handleJobTask||params={}", params);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("get early dsl template");

        // 获取一天前的查询模板信息
        List<DslTemplatePO> dslTemplatePOList = dslTemplateESDAO.getEarliestDslTemplate();

        // 获取重要索引模板信息
        List<IndexTemplatePhyWithLogic> indexTemplatePhyWithLogicList = Lists.newArrayList();
        IndexTemplatePhyWithLogic dosOrder = new IndexTemplatePhyWithLogic();
        dosOrder.setName("dos_order");
        dosOrder.setExpression("dos_order*");
        indexTemplatePhyWithLogicList.add(dosOrder);

        if (!CollectionUtils.isEmpty(dslTemplatePOList)) {

            stopWatch.stop().start("search history and save");
            String ariusModifyTime = DateTimeUtil.getCurrentFormatDateTime();

            Iterator<DslTemplatePO> iterator = dslTemplatePOList.iterator();
            while (iterator.hasNext()) {
                getQpsThenUpdateQueryLimit(iterator, indexTemplatePhyWithLogicList, ariusModifyTime);
            }

            boolean operatorResult = dslTemplateESDAO.updateTemplates(dslTemplatePOList);
            String cost = stopWatch.stop().toString();
            LOGGER.info("class=DslTemplateUpdateEarliestQueryLimitJob||method=handleJobTask||msg=update earliest dsl template {} operatorResult {}, cost {}",
                    dslTemplatePOList.size(), operatorResult, cost);

        } else {
            String cost = stopWatch.stop().toString();
            LOGGER.info("class=DslTemplateUpdateEarliestQueryLimitJob||method=handleJobTask||msg=update earliest dsl template is empty, cost {}", cost);
        }

        return JOB_SUCCESS;
    }

    /**
     * 获取qps值然后更新限流值
     *
     * @param iterator
     */
    private void getQpsThenUpdateQueryLimit(Iterator<DslTemplatePO> iterator, List<IndexTemplatePhyWithLogic> indexTemplatePhyWithLogicList, String ariusModifyTime) {
        DslTemplatePO dslTemplatePO = iterator.next();

        double searchCountPreSecond = getMaxQpsByAppidTemplateMd5(dslTemplatePO);
        double increaseFactor = getIncreaseFactor(dslTemplatePO, indexTemplatePhyWithLogicList);
        // 查询限流值最小为MIN_QUERY_LIMIT
        double queryLimitValue = Math.max(MIN_QUERY_LIMIT, searchCountPreSecond * increaseFactor);

        if (dslTemplatePO.getQueryLimit() != null && 0 == Double.compare(dslTemplatePO.getQueryLimit(), queryLimitValue)) {
            // 如果查询限流值没发生变化，就不需要更新es
            iterator.remove();
        } else {
            // 保留两位小数
            queryLimitValue = (double) Math.round(queryLimitValue * 100) / 100;
            dslTemplatePO.setQueryLimit(queryLimitValue);
            dslTemplatePO.setAriusModifyTime(ariusModifyTime);
        }
    }

    /**
     * 获取增长因子，范围在1.1到2之间
     *
     * @param dslTemplatePO
     * @param indexTemplatePhyWithLogicList
     * @return
     */
    private double getIncreaseFactor(DslTemplatePO dslTemplatePO, List<IndexTemplatePhyWithLogic> indexTemplatePhyWithLogicList) {
        double increaseFactor = FACTOR;

        // 如果查询使用的索引名称不为空，则匹配是否为核心索引
        if (StringUtils.isNotBlank(dslTemplatePO.getIndices())) {
            String[] indexNameArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(dslTemplatePO.getIndices(), ",");
            for (String indexName : indexNameArray) {
                // 根据索引名称找到对应模板
                Set<String> matchIndexTemplateNameSet = IndexNameUtils.matchIndexTemplateBySearchIndexName(indexName, indexTemplatePhyWithLogicList);
                if (matchIndexTemplateNameSet.isEmpty()) {
                    increaseFactor = 2.0f;
                } else {
                    // 重要索引，查询增长因子为1.5
                    increaseFactor = 1.5f;
                    break;
                }
            }
        }

        // 根据查询影响情况计算查询增长因子
        if (!checkDslTemplateParam(dslTemplatePO)) {
            return increaseFactor;
        }

        // 如果为聚合则增长因子*0.8
        if ("aggs".equals(dslTemplatePO.getDslType())) {
            increaseFactor *= 0.8f;
        }
        // 平均查询耗时大于1s则增长因子*0.8
        if (dslTemplatePO.getTotalCostAvg() > 1000) {
            increaseFactor *= 0.8f;
        }
        // 平均查询shard数量大于200则增长因子*0.8
        if (dslTemplatePO.getTotalShardsAvg() > 200) {
            increaseFactor *= 0.8f;
        }

        // 范围在1.1到2之间
        increaseFactor = Math.max(increaseFactor, 1.1f);

        return increaseFactor;
    }

    /**
     * 获取最大qps
     *
     * @param dslTemplatePO
     * @return
     */
    private double getMaxQpsByAppidTemplateMd5(DslTemplatePO dslTemplatePO) {
        DslBase dslBase = new DslBase(dslTemplatePO.getAppid(), dslTemplatePO.getDslTemplateMd5());
        // 获取指定appid和templateMD5的历史查询量记录
        DslAnalyzeResultQpsPO dslAnalyzeResultQpsPO = dslAnalyzeResultQpsESDAO.getMaxAppIdTemplateQpsInfoByAppIdTemplateMd5(dslBase);

        double searchCountPreSecond;
        // 如果没有得到查询结果，从分钟级别数据
        if (null == dslAnalyzeResultQpsPO) {
            long searchCountPerMin;
            // 从分钟级别数据
            DslMetricsPO dslMetricsPO = dslMetricsESDAO.getMaxAppidTemplateQpsInfoByAppidTemplateMd5(dslBase);
            // 如果没有得到查询结果，默认限流值
            if (null == dslMetricsPO) {
                LOGGER.error("class=DslTemplateUpdateEarliestQueryLimitJob||method=handleJobTask||errMsg=calQueryLimit {} has no metrics content",
                        dslTemplatePO.getKey());
                searchCountPerMin = 10L;
            } else {
                searchCountPerMin = dslMetricsPO.getSearchCount();
            }

            // 如果没有历史的查询数量，默认限流值
            if (searchCountPerMin <= 0) {
                LOGGER.error("class=DslTemplateUpdateEarliestQueryLimitJob||method=handleJobTask||errMsg=calQueryLimit {}, searchCount {} set default queryEsByClusterName limit",
                        dslTemplatePO.getKey(), searchCountPerMin);
                searchCountPerMin = 10L;
            }

            searchCountPreSecond = searchCountPerMin / 60.0;
        } else {
            searchCountPreSecond = dslAnalyzeResultQpsPO.getSearchCount();
        }

        return searchCountPreSecond;
    }

    /**
     * dslTemplatePO参数校验
     *
     * @param dslTemplatePO
     * @return
     */
    private boolean checkDslTemplateParam(DslTemplatePO dslTemplatePO) {
        if (null == dslTemplatePO.getTotalCostAvg()) {
            return false;
        } else if (null == dslTemplatePO.getTotalShardsAvg()) {
            return false;
        } else if (null == dslTemplatePO.getTotalHitsAvg()) {
            return false;
        } else if (null == dslTemplatePO.getResponseLenAvg()) {
            return false;
        } else if (StringUtils.isBlank(dslTemplatePO.getDslType())) {
            return false;
        } else {
            return true;
        }
    }
}
