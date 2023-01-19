package com.didichuxing.datachannel.arius.admin.persistence.es.metric;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.GatewayMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.hits.ESHits;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * top n 指标采集dao
 *
 * @author shizeying
 * @date 2022/05/10
 */
public abstract class BaseTopNMetricsDAO extends BaseESDAO {
    @Value("${es.update.cluster.name}")
    protected              String metadataClusterName;
    protected static final ILog   LOGGER     = LogFactory.getLog(BaseTopNMetricsDAO.class);
    protected              String indexName;
    protected static final String TYPE       = "type";
    protected static final String TIMESTAMP  = "timeStamp";
    protected static final String TERM       = "term";
    protected static final String TERMS      = "terms";
    protected static final String PROJECT_ID = "projectId";
    protected static final String VALUE      = "value";
    protected static final String EMPTY_STR  = "";
    protected static final String BUCKETS    = "buckets";
    
    /**
     * 执行具体的top N 拉取查询 *
     *
     * @param gatewayMetricsTypeEnum 网关指标类型枚举
     * @param startTime              开始时间
     * @param endTime                结束时间
     * @param func                   函数
     * @param hasDataTimeDsl         数据时间dsl
     * @param metricValue            指标值
     * @param params                 传入方式参照 *
     *                               {@link BaseTopNMetricsDAO#getFinalDslByOneStep(GatewayMetricsTypeEnum, Object[])}
     * @return {@link List}<{@link VariousLineChartMetrics}>
     */
    protected <R> List<R> performFetchTopMetric(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime,
                                                Long endTime, Function<ESQueryResponse, List<R>> func,
                                                String hasDataTimeDsl, String metricValue,Object... params) throws AdminOperateException {
        //校验指标值是否存在
        final List<R> metrics = checkMetricsValue(gatewayMetricsTypeEnum, metricValue);
        if (CollectionUtils.isNotEmpty(metrics)) {
            return metrics;
        }
        final Long timePoint = getGatewayHasDataTimeByField(startTime, endTime, hasDataTimeDsl);    // 查询剪支
        if (Objects.isNull(timePoint)) {
            return Collections.emptyList();
        }
        Tuple<Long, Long> firstInterval = MetricsUtils.getSortInterval(endTime - startTime, timePoint);
        long startInterval = firstInterval.getV1();
        long endInterval = MetricsUtils.getDailyNextOneMinute(startInterval);
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startInterval, endInterval);
        final List<Object> arrayList = Lists.newArrayList(params);
        arrayList.add(0, startInterval);
        arrayList.add(1, endInterval);
        final Object[] args = arrayList.toArray();
        //同时匹配多种枚举类型
        String dsl = getFinalDslByOneStep(gatewayMetricsTypeEnum , args);
        //TODO performRequest出错未处理，返回结果为NULL，需要优化
        return gatewayClient.performRequest(metadataClusterName, realIndexName, TYPE, dsl, func, 3);
    }
    
    /**
     * 对具体的{@link  GatewayMetricsTypeEnum} 进行召回 *
     *
     * @param gatewayMetricsTypeEnum 网关指标类型枚举
     * @param startTime              开始时间 用于计算真实查询的索引列表
     * @param endTime                结束时间 用于计算真实查询的索引列表
     * @param func                   函数
     * @param params                 参数个数 传入方式参照
     *                               {@link BaseTopNMetricsDAO#getFinalDslBySecondStep(GatewayMetricsTypeEnum, Object[])} *
     *                               @return {@link List}<{@link MetricsContent}>
     */
    protected List<MetricsContent> performGetByRangeTopN(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime,
                                                         Long endTime,
                                                         Function<ESQueryResponse, List<MetricsContent>> func,
                                                         Object... args)
            throws AdminOperateException {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String dsl = getFinalDslBySecondStep(gatewayMetricsTypeEnum , args);
        if (Objects.isNull(dsl)) {
            return Collections.emptyList();
        }
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, func, 3);
    }
    

    
    /**
     * 网关有时间数据字段 *
     *
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @param hasDataTimeDsl 数据时间dsl
     * @return {@link Long}
     */
    private Long getGatewayHasDataTimeByField(Long startTime, Long endTime, String hasDataTimeDsl) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        return gatewayClient.performRequest(metadataClusterName, realIndexName, TYPE, hasDataTimeDsl,
                response -> Optional.ofNullable(response).map(ESQueryResponse::getHits).map(ESHits::getHits)
                        .filter(CollectionUtils::isNotEmpty)
                        //由于过滤出来不为空的hits，那么我们只需要获取第一个值即可，这就是我们想要的hit，请注意这里不会产生NPE异常
                        .map(hits -> hits.get(0).getSource()).map(hit -> ((JSONObject) hit).getLongValue(TIMESTAMP))
                        .orElse(null), 3);
    }
    
    /**
     * 判断project id 是否存在并进行构建
     * <pre>
     *     {
     *     "term": {
     *     "projectId": {
     *     "value": %d
     *           }
     *           }
     *           }
     *           </pre>  or
     * <pre> ""     </pre>
     *
     * @param projectId projectId
     * @return {@link String}
     */
    protected String buildTermByProjectId(Integer projectId) {
        return Objects.nonNull(projectId)
                ? "," + new JSONObject().fluentPut(TERM,
                new JSONObject().fluentPut(PROJECT_ID, new JSONObject().fluentPut(VALUE, projectId))).toJSONString()
                : EMPTY_STR;
    }
    
    /**
     * 这里不带,的terms
     * <pre>
     *      {
     *           "terms": {
     *             "FIELD": [
     *               "VALUE1",
     *               "VALUE2"
     *             ]
     *           }
     *         }
     * </pre>
     *
     * @param field
     * @param values 值
     * @return {@link String}
     */
    protected String buildTermsByField(String field, List<Object> values) {
        return new JSONObject().fluentPut(TERMS, new JSONObject().fluentPut(field, values)).toJSONString();
    }
    
    /**
     * 构建第一阶段查询的dsl
     *
     * @param gatewayMetricsTypeEnum 网关指标类型枚举
     * @param args
     * @return {@link String}
     */
    protected abstract String getFinalDslByOneStep(GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                                   Object[] args);
    
    protected abstract String getFinalDslBySecondStep(GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                                      Object[] args);
    
    /**
     * 获取指定范围的指标top n *
     *
     * @param values                 第一阶段召回的结果
     * @param gatewayMetricsTypeEnum 网关指标类型枚举
     * @param startTime              开始时间 *
     * @param endTime                结束时间
     * @param projectId              projectId
     * @return {@link List}<{@link MetricsContent}>
     */
    public abstract List<MetricsContent> getByRangeTopN(List<String> values,
                                                        GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime,
                                                        Long endTime, Integer projectId) throws AdminOperateException;
    
    /**
     * 检查指标value,如果存在则返回{@link Collections#singleton(R)}，如果不存在则则返回{@link Collections#emptyList()} * * @param
     * gatewayMetricsTypeEnum 网关指标类型枚举 * @param metricsValue 指标value * @return {@link List}<{@link R}>
     */
    public abstract <R> List<R> checkMetricsValue(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String metricsValue);
    
    /**
     * 获取最高规 *
     *
     * @param gatewayMetricsTypeEnum 网关指标类型枚举
     * @param startTime              开始时间
     * @param endTime                结束时间
     * @param topNu                  top n
     * @param projectId              projectId
     * @param value                  value 指标值 *
     * @return {@link List}<{@link VariousLineChartMetrics}>
     */
    public abstract <R> List<R> fetchTopMetric(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime,
                                               Long endTime, Integer topNu, Integer projectId, String value)
            throws AdminOperateException;
}