package com.didichuxing.datachannel.arius.admin.metadata.job.query;

import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslAnalyzeResultQpsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslMetricsPO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslAnalyzeResultQpsESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslMetricsESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayJoinESDAO;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.ListUtils;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Callable;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/1/22 下午3:53
 * @modified By D10865
 *
 * 采集一天指定appid和dslTemplateMd5最大qps
 */
@Component
public class QueryMaxQpsLimitCollector extends AbstractMetaDataJob {

    /**
     * 操作dsl metrics 索引
     */
    @Autowired
    private DslMetricsESDAO dslMetricsEsDao;
    /**
     * 操作gateway join 索引
     */
    @Autowired
    private GatewayJoinESDAO gatewayJoinEsDao;
    /**
     * 操作dsl.analyze.result 索引
     */
    @Autowired
    private DslAnalyzeResultQpsESDAO dslAnalyzeResultQpsEsDao;
    /**
     * 任务线程池
     */
    private static final FutureUtil futureUtil = FutureUtil.init("QueryMaxQpsLimitCollector");

    /**
     * 处理采集任务
     *
     * @return
     */
    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=QueryMaxQpsLimitCollector||method=handleJobTask||params={}", params);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("get apppid");

        // 计算起始结束时间范围
        Tuple<String, String> stringStringTuple = DateTimeUtil.getStartEndTimeByDate("");
        String startDate = stringStringTuple.v1();
        String endDate   = stringStringTuple.v2();

        // 获取指定时间去重的查询模板信息
        List<DslMetricsPO> appidTemplateList = dslMetricsEsDao.getAppIdTemplateMd5InfoByDate(startDate);

        LOGGER.info("class=QueryMaxQpsLimitCollector||method=handleJobTask||msg=startDate -> {} ,endDate -> {}, appidTemplateList count -> {}", startDate, endDate, appidTemplateList.size());

        stopWatch.stop().start("get qps");
        List<DslAnalyzeResultQpsPO> dslAnalyzeResultQpsPOList = getAggSearchCountResult(appidTemplateList, startDate);
        stopWatch.stop().start("save result");

        boolean operatorResult = dslAnalyzeResultQpsEsDao.bathInsert(dslAnalyzeResultQpsPOList);

        LOGGER.info("class=QueryMaxQpsLimitCollector||method=handleJobTask||msg=finish result {}, cost {}", operatorResult, stopWatch.stop().toString());

        return JOB_SUCCESS;
    }

    /**
     * 获取聚合后查询次数
     *
     * @param appidTemplateList
     * @param startDate
     * @return
     */
    private List<DslAnalyzeResultQpsPO> getAggSearchCountResult(List<DslMetricsPO> appidTemplateList, String startDate) {
        List<DslAnalyzeResultQpsPO> dslAnalyzeResultQpsPOList = Lists.newArrayList();
        if (appidTemplateList == null || appidTemplateList.isEmpty()) {
            return dslAnalyzeResultQpsPOList;
        }

        // 多线程并发查询
        int taskCount = 4;
        int groupSize = (int)Math.ceil(appidTemplateList.size() / taskCount);
        List<List<DslMetricsPO>> appidTemplateLists = ListUtils.partition(appidTemplateList, groupSize);

        for (List<DslMetricsPO> subList : appidTemplateLists) {
            LOGGER.info("class=QueryMaxQpsLimitCollector||method=getAggSearchCountResult||msg=submit task subList size -> {}", subList.size());

            futureUtil.callableTask((Callable<List<DslAnalyzeResultQpsPO>>) () -> {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                List<DslAnalyzeResultQpsPO> appidTemplateQpsInfoList = Lists.newArrayList();

                long recordCount = 0;
                for (DslMetricsPO dslMetricsPO : subList) {
                    ++recordCount;
                    if (recordCount % 50 == 0) {
                        LOGGER.info("class=QueryMaxQpsLimitCollector||method=getAggSearchCountResult||msg=recordCount {}/{}", recordCount, subList.size());
                    }

                    Tuple<Long, Long> maxQpsTimeTuple = gatewayJoinEsDao.queryMaxSearchQpsByAppIdAndDslTemplate(startDate, dslMetricsPO.getAppid(), dslMetricsPO.getDslTemplateMd5());
                    if (maxQpsTimeTuple != null) {
                        appidTemplateQpsInfoList.add(DslAnalyzeResultQpsPO.buildAppIdTemplateQpsInfo(maxQpsTimeTuple, dslMetricsPO.getAppid(), dslMetricsPO.getDslTemplateMd5(), startDate));
                    } else {
                        LOGGER.info("class=QueryMaxQpsLimitCollector||method=getAggSearchCountResult||msg={}, {} not access info in arius.gateway.join {} index", dslMetricsPO.getAppid(), dslMetricsPO.getDslTemplateMd5());
                    }
                }
                stopWatch.stop();
                LOGGER.info("class=QueryMaxQpsLimitCollector||method=getAggSearchCountResult||msg=agg qps {} cost {}", subList.size(), stopWatch.toString());
                return appidTemplateQpsInfoList;
            });
        }

        dslAnalyzeResultQpsPOList.addAll(futureUtil.waitResult());

        return dslAnalyzeResultQpsPOList;
    }
}
