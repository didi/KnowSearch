package com.didichuxing.datachannel.arius.admin.metadata.job.appid;

import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslAnalyzeResultTypePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayJoinPO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslAnalyzeResultTypeESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslTemplateESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayJoinESDAO;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.*;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_FAILED;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/1/22 下午5:20
 * @modified By D10865
 *
 * 应用查询情况采集器
 */
@Component
public class AppIdQueryCollector extends AbstractMetaDataJob {

    @Autowired
    private AppService appService;

    /**
     * 操作dsl template 索引
     */
    @Autowired
    private DslTemplateESDAO dslTemplateEsDao;
    /**
     * 操作gateway join 索引
     */
    @Autowired
    private GatewayJoinESDAO gatewayJoinEsDao;
    /**
     * 操作dsl analyze result 索引
     */
    @Autowired
    private DslAnalyzeResultTypeESDAO dslAnalyzeResultTypeEsDao;

    /**
     * 处理采集任务
     *
     * @return
     */
    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=AppIdQueryCollector||method=handleJobTask||params={}", params);

        List<App> queryAppList = appService.listApps();
        if (CollectionUtils.isEmpty(queryAppList)) {
            LOGGER.error("class=AppIdQueryCollector||method=handleJobTask||params={}||errMsg=appid list response is empty",
                    params);
            return JOB_FAILED;
        }

        boolean operatorResult = analyzeAppIdQueryInfoThenStoreResult(queryAppList, params);

        LOGGER.info("class=AppIdQueryCollector||method=handleJobTask||msg=operatorResult {}, queryAppList size {}",
                operatorResult, queryAppList.size());

        return JOB_SUCCESS;
    }

    /**************************************************** private method ****************************************************/
    /**
     * 分析appid查询信息然后保存到es
     *
     * @param queryAppList
     * @param date
     * @return
     */
    private boolean analyzeAppIdQueryInfoThenStoreResult(List<App> queryAppList, String date) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("");
        List<DslAnalyzeResultTypePO> analyzeResultList = Lists.newArrayList();

        Tuple<String,String> stringStringTuple = DateTimeUtil.getStartEndTimeByDate(date);
        String startDate = stringStringTuple.v1();
        String endDate   = stringStringTuple.v2();

        LOGGER.info("class=AppIdQueryCollector||method=analyzeAppIdQueryInfoThenStoreResult||startDate={}||endDate={}",
                startDate, endDate);
        DslAnalyzeResultTypePO po = null;

        for (App app : queryAppList) {

            try {
                int appId = app.getId();
                LOGGER.info("class=AppIdQueryCollector||method=analyzeAppIdQueryInfoThenStoreResult||appid={}||msg=start",
                        appId);

                DslTemplates dslTemplates = getDslTemplates(appId, startDate, endDate);
                // 获取正常查询概要
                SearchOverview searchOverview = getSearchOverview(appId, startDate);
                // 获取慢查DSL信息
                SlowDsls slowDsls = getSlowDsls(appId, startDate);
                // 获取异常查询信息
                ErrorDsls errorDsls = getErrorDsls(appId, startDate);
                // 获取访问gateway分布情况
                String accessGatewayInfo = gatewayJoinEsDao.getAccessGatewayInfoByAppidDate(appId, startDate);

                po = new DslAnalyzeResultTypePO();
                po.setAppid(appId);
                po.setDate(startDate);
                po.setAccessGatewayInfo(accessGatewayInfo);
                po.setDslTemplates(dslTemplates);
                po.setOverview(searchOverview);
                po.setSlowDsls(slowDsls);
                po.setErrorDsls(errorDsls);
                po.setAriusType("appid query");

                analyzeResultList.add(po);

                LOGGER.info("class=AppIdQueryCollector||method=analyzeAppIdQueryInfoThenStoreResult||appid={}||msg=finish",
                        appId);
            } catch (Exception e) {
                LOGGER.error("class=AppIdQueryCollector||method=analyzeAppIdQueryInfoThenStoreResult||appid={}||errMsg=fail to get. ",
                        app.getId(), e);
            }
        }

        stopWatch.stop();
        LOGGER.info("class=AppIdQueryCollector||method=analyzeAppIdQueryInfoThenStoreResult||msg=analyzeResultList size -> {}||cost={}",
                analyzeResultList.size(), stopWatch.toString());

        return dslAnalyzeResultTypeEsDao.batchInsert(analyzeResultList);
    }

    /**
     * 获取dsl查询模板信息
     *
     * @param appid
     * @param startDate
     * @param endDate
     * @param
     * @return
     */
    private DslTemplates getDslTemplates(Integer appid, String startDate, String endDate) {
        DslTemplates dslTemplates = new DslTemplates();
        // 获取查询模板总数
        Long dslTemplateTotalCount = dslTemplateEsDao.getTemplateCountByAppId(appid);
        // 新增查询模板个数
        Long increaseDslTemplateTotalCount = dslTemplateEsDao.getIncreaseTemplateCountByAppId(appid, startDate, endDate);

        dslTemplates.setDslTotalCnt(dslTemplateTotalCount);
        dslTemplates.setDslIncCnt(increaseDslTemplateTotalCount);

        return dslTemplates;
    }

    /**
     * 获取正常查询概要
     *
     * @param appid
     * @param startDate
     */
    private SearchOverview getSearchOverview(Integer appid, String startDate) {
        SearchOverview searchOverview = new SearchOverview();
        // 获取查询次数
        Long searchTotalCount = gatewayJoinEsDao.getTotalSearchCountByAppidAndDate(appid, startDate);
        // 查询qps信息
        QueryQpsMetric queryQpsMetric = gatewayJoinEsDao.getQpsInfoByAppidAndDate(appid, startDate);
        // 查询耗时信息
        Map<String, Object> totalCostMap = gatewayJoinEsDao.getCostInfoByAppidAndDate(appid, startDate);

        searchOverview.setCount(searchTotalCount);
        searchOverview.setQpsMetric(queryQpsMetric);
        searchOverview.setCostQuantile(totalCostMap);

        return searchOverview;
    }

    /**
     * 获取慢查DSL信息
     *
     * @param appid
     * @param startDate
     */
    private SlowDsls getSlowDsls(Integer appid, String startDate) {
        SlowDsls slowDsls = new SlowDsls();
        SlowQueryInfo slowQueryInfo = null;
        long slowSearchCount = 0L;
        long slowDslThreshold;
        GatewayJoinPO gatewayJoinPO = null;
        // 分析慢查原因
        List<SlowQueryInfo> slowQueryInfoList = Lists.newArrayList();

        // 获取这个appid的所有查询模板及慢查阈值
        List<DslTemplatePO> userDslTemplates = dslTemplateEsDao.getAllDslTemplatePOByAppid(appid);
        for (DslTemplatePO DslTemplatePO : userDslTemplates) {

            if (null == DslTemplatePO.getSlowDslThreshold()) {
                slowDslThreshold = 1000L;
            } else {
                slowDslThreshold = DslTemplatePO.getSlowDslThreshold();
            }
            // 或者这个查询模板慢查次数及一条记录
            Tuple<Long, GatewayJoinPO> gatewayJoinTuple = gatewayJoinEsDao.querySlowDslCountAndDetailByByAppidAndDslTemplate(startDate, appid, DslTemplatePO.getDslTemplateMd5(),
                    slowDslThreshold);
            if (gatewayJoinTuple == null) {
                continue;
            }

            slowSearchCount += gatewayJoinTuple.v1();
            gatewayJoinPO = gatewayJoinTuple.v2();

            if (gatewayJoinPO == null) {
                continue;
            }

            slowQueryInfo = new SlowQueryInfo();
            slowQueryInfo.setCount(gatewayJoinTuple.v1());
            slowQueryInfo.setDslTemplateMd5(DslTemplatePO.getDslTemplateMd5());
            slowQueryInfo.setDslTemplate(gatewayJoinPO.getDslTemplate());
            slowQueryInfo.setDsl(gatewayJoinPO.getDsl());
            slowQueryInfo.setCost(1.0 * gatewayJoinPO.getTotalCost());
            slowQueryInfo.setIndices(gatewayJoinPO.getIndices());
            slowQueryInfo.setSlowDslThreshold(slowDslThreshold);

            // 判断该查询模板查询耗时与历史查询模板耗时情况对比，逐渐完善
            if (gatewayJoinPO.getTotalCost() >= DslTemplatePO.getTotalCostAvg()) {
                slowQueryInfo.setCause("查询语句原因");
                slowQueryInfo.setSlowReasonType(SlowDslReasonType.USER_DSL);
            } else {
                slowQueryInfo.setCause("ES 原因");
                slowQueryInfo.setSlowReasonType(SlowDslReasonType.ES);
            }
            slowQueryInfoList.add(slowQueryInfo);
        }
        slowDsls.setCount(slowSearchCount);
        slowDsls.setDetails(slowQueryInfoList);

        return slowDsls;
    }

    /**
     * 获取异常查询信息
     *
     * @param appid
     * @param startDate
     * @return
     */
    private ErrorDsls getErrorDsls(Integer appid, String startDate) {
        ErrorDsls errorDsls = new ErrorDsls();
        List<ErrorDslInfo> errorDslInfoList = Lists.newArrayList();
        ErrorDslInfo errorDslInfo;
        List<ErrorDslDetail> details;

        long totalCount = 0L;
        // 获取查询异常个数和异常
        Tuple<Long, List<Tuple<String, Long>>> errorSearchTuple = gatewayJoinEsDao.getErrorSearchCountAndErrorDetailByAppidDate(appid, startDate);

        if (errorSearchTuple != null) {
            totalCount = errorSearchTuple.v1();
            for (Tuple<String, Long> tuple : errorSearchTuple.v2()) {
                Long docCount = tuple.v2();

                if (docCount != null && docCount != 0) {
                    errorDslInfo = new ErrorDslInfo();
                    String exceptionName = tuple.v1();
                    errorDslInfo.setCount(docCount);
                    errorDslInfo.setName(exceptionName);

                    details = Lists.newArrayList();
                    errorDslInfo.setDetails(details);

                    // 获取指定异常的具体查询dsltemplateMD5及次数
                    getErrorDslDetail(appid, startDate, details, exceptionName);

                    errorDslInfoList.add(errorDslInfo);
                }

            }
        }

        errorDsls.setCount(totalCount);
        errorDsls.setDetails(errorDslInfoList);

        return errorDsls;
    }

    private void getErrorDslDetail(Integer appid, String startDate, List<ErrorDslDetail> details, String exceptionName) {
        ErrorDslDetail errorDslDetail;
        Map<String, Long> errorDslMap = gatewayJoinEsDao.queryErrorDslByAppidExceptionAndDate(startDate, appid, exceptionName);
        for (Map.Entry<String, Long> entry : errorDslMap.entrySet()) {
            GatewayJoinPO gatewayJoinPO = gatewayJoinEsDao.queryErrorDslDetailByAppidTemplateAndDate(startDate, appid, entry.getKey(), exceptionName);
            if (gatewayJoinPO == null) {
                LOGGER.error("class=AppIdQueryCollector||method=getErrorDsls||appid={}||errMsg=can't find error detail {} in arius.gateway.join {}",
                        appid, entry.getKey(), startDate);
                continue;
            }
            errorDslDetail = new ErrorDslDetail();
            errorDslDetail.setCount(entry.getValue());
            errorDslDetail.setIndices(gatewayJoinPO.getIndices());
            errorDslDetail.setDsl(gatewayJoinPO.getDsl());
            errorDslDetail.setDslTemplateMd5(entry.getKey());
            errorDslDetail.setDslTemplate(gatewayJoinPO.getDslTemplate());

            details.add(errorDslDetail);
        }
    }
}
