package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.AVG;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.DOUBLE;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.FIELD;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.HIST;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.INT;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.KEY;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.LONG;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.METRICS;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.PERCENTILES;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.STATIS;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.TIME_STAMP;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.VALUE;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.VALUES;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.TopMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.ESClusterTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.PercentilesEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.OneLevelTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

@NoArgsConstructor
public class BaseAriusStatsESDAO extends BaseESDAO {

    @Value("${es.metrics.cluster.buckets.max.num}")
    protected int                                                         clusterMaxNum;

    @Value("${es.metrics.nodes.buckets.max.num}")
    protected int                                                         esNodesMaxNum;

    @Value("${es.update.cluster.name}")
    protected String                                                      metadataClusterName;

    @Value("${es.metrics.indices.buckets.max.num}")
    protected int                                                         indicesBucketsMaxNum;
    /**
     * 操作的索引名称
     */
    protected String                                                      indexName;

    /**
     * 索引type名称为type
     */
    protected static final String                                         TYPE               = "type";

    protected static final int                                            SCROLL_SIZE        = 5000;
    protected static final Long                                           ONE_GB             = 1024 * 1024 * 1024L;

    protected static final String                                         NOW_7M             = "now-7m";
    protected static final String                                         NOW_6M             = "now-6m";
    protected static final String                                         NOW_5M             = "now-5m";
    protected static final String                                         NOW_4M             = "now-4m";
    protected static final String                                         NOW_2M             = "now-2m";
    protected static final String                                         NOW_3M             = "now-3m";
    protected static final String                                         NOW_1M             = "now-1m";
    protected static final String                                         DEFAULT_AGG        = "avg";
    public static final String                                            INDEX_INDEX_TOTAL_DIFF = "indexing-index_total";
    public static final String                                            SEARCH_QUERY_TOTAL_DIFF = "search-query_total";

    /**
     * 不同维度es监控数据
     */
    private static Map<AriusStatsEnum/*stats type*/, BaseAriusStatsESDAO> ariusStatsEsDaoMap    = Maps
        .newConcurrentMap();

    public static BaseAriusStatsESDAO getByStatsType(AriusStatsEnum statsType) {
        return ariusStatsEsDaoMap.get(statsType);
    }

    /**
     * 注册不同维度数据对应操作的es类
     *
     * @param statsType
     * @param baseAriusStatsEsDao
     */
    public static void register(AriusStatsEnum statsType, BaseAriusStatsESDAO baseAriusStatsEsDao) {
        ariusStatsEsDaoMap.put(statsType, baseAriusStatsEsDao);
    }

    /**
     * 批量插入索引统计信息

     * @param statsInfo
     * @return
     */
    public boolean batchInsertStats(List<? extends BaseESPO> statsInfo) {
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);
        return updateClient.batchInsert(realIndex, TYPE, statsInfo);
    }

    /**
     * 从es返回结果中得到求和值
     *
     * @param response
     * @param sumKey
     * @return
     */
    Double getSumFromESQueryResponse(ESQueryResponse response, String sumKey) {
        if (null == response || response.getAggs() == null) {
            LOGGER.warn("class=BaseAriusStatsEsDao||method=getSumFromESQueryResponse||msg=response is null");
            return 0d;
        }

        String value = null;
        try {
            Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();

            if (esAggrMap != null && esAggrMap.containsKey(sumKey) && esAggrMap.get(sumKey).getUnusedMap() != null
                && esAggrMap.get(sumKey).getUnusedMap().get("value") != null) {
                value = esAggrMap.get(sumKey).getUnusedMap().get("value").toString();
                return Double.valueOf(value);
            }

        } catch (Exception e) {
            LOGGER.error(
                "class=BaseAriusStatsEsDao||method=getSumFromESQueryResponse||sumKey={}||value={}||response={}",
                sumKey, value, response, e);
        }
        return 0d;
    }

    Map<String, Double> getAvgAndPercentilesFromESQueryResponse(ESQueryResponse response) {
        Map<String, Double> percentiles2ValueMap = Maps.newHashMap();
        if (null == response || null == response.getAggs()) {
            LOGGER.warn("class=BaseAriusStatsEsDao||method=getAvgAndPercentilesFromESQueryResponse||msg=response is null");
            return percentiles2ValueMap;
        }

        try {
            Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();
            if (null != esAggrMap) {
                //构建分位值
                setPercentiles(percentiles2ValueMap, esAggrMap);

                //构建平均值
                if (null != esAggrMap.get(AVG) && null != esAggrMap.get(AVG).getUnusedMap()
                        && null != esAggrMap.get(AVG).getUnusedMap().get(VALUE)) {
                    Map<String, Object> avgMapFromES = esAggrMap.get(AVG).getUnusedMap();
                    percentiles2ValueMap.put(AVG, Double.valueOf(avgMapFromES.get(VALUE).toString()));
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=BaseAriusStatsEsDao||method=getAvgAndPercentilesFromESQueryResponse||response={}", response, e);
        }

        return percentiles2ValueMap;
    }

    /**
     * 生成查询的索引名称，默认7天
     *
     * @param indexCount
     * @return
     */
    String genIndexNames(Integer indexCount) {
        try {
            if (indexCount == null) {
                indexCount = 7;
            }

            List<String> indices = Lists.newArrayList();

            for (int day = 0; day < indexCount; day++) {
                String realIndexName = this.indexName.concat("_").concat(DateTimeUtil.getFormatDayByOffset(day))
                    .concat("*");
                indices.add(realIndexName);
            }
            return StringUtils.join(indices, ",");

        } catch (Exception e) {
            LOGGER.error(
                "class=BaseAriusStatsEsDao||method=genIndexNames||errMsg=gen last 7 days index names error.||stack={}",
                e);
        }

        // 异常时查询所有时间段的索引
        return this.indexName.concat("*");
    }

    <T> List<T> fetchAggClusterPhyMetrics(ESQueryResponse response, Class<T> clazz) {
        List<T> aggMetrics = Lists.newCopyOnWriteArrayList();

        if (response == null || response.getAggs() == null) {
            return aggMetrics;
        }

        try {
            Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();
            if (null != esAggrMap && null != esAggrMap.get(HIST)) {
                esAggrMap.get(HIST).getBucketList().parallelStream().forEach(r -> {

                    T obj;
                    long timeStamp = 0;
                    try {
                        obj = clazz.newInstance();
                        if (null != r.getUnusedMap() && null != r.getUnusedMap().get(KEY)) {
                            timeStamp = Long.valueOf(r.getUnusedMap().get(KEY).toString());
                        }
                        handleFields(r, obj, timeStamp);
                        aggMetrics.add(obj);
                    } catch (Exception e) {
                        LOGGER.error(
                                "class=BaseAriusStatsESDAO||method=fetchAggClusterPhyMetrics||errMsg=exception! response:{}",
                                response.toString(), e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error(
                "class=BaseAriusStatsESDAO||method=fetchAggClusterPhyMetrics||errMsg=exception! response:{}",
                response.toString(), e);
        }
        return aggMetrics;
    }

    private <T> void handleFields(ESBucket eSBucket, T obj, long timeStamp) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        for (Field field : Objects.requireNonNull(obj).getClass().getDeclaredFields()) {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), obj.getClass());
            //1. get method of setter
            Method writeMethod = pd.getWriteMethod();
            Class<?>[] parameterTypes = writeMethod.getParameterTypes();

            //获取时间戳
            if (TIME_STAMP.equals(field.getName()) && parameterTypes.length > 0) {
                String type = parameterTypes[0].getName();
                if (LONG.equals(type)) {
                    writeMethod.invoke(obj, timeStamp);
                    continue;
                }
            }

            //获取统计数据
            handleStatisticalData(eSBucket, obj, field, writeMethod, parameterTypes);
        }
    }

    private <T> void handleStatisticalData(ESBucket eSBucket, T obj, Field field, Method writeMethod, Class<?>[] parameterTypes) throws IllegalAccessException, InvocationTargetException {
        String metricKey = field.getName();
        if (null != eSBucket.getAggrMap() && null != eSBucket.getAggrMap().get(metricKey)
            && null != eSBucket.getAggrMap().get(metricKey).getUnusedMap().get(VALUE)
            && parameterTypes.length > 0) {
            String type = parameterTypes[0].getName();
            if (DOUBLE.equals(type)) {
                writeMethod.invoke(obj, Double.valueOf(
                    eSBucket.getAggrMap().get(metricKey).getUnusedMap().get(VALUE).toString()));
            }
            if (LONG.equals(type)) {
                writeMethod.invoke(obj,
                    Double.valueOf(eSBucket.getAggrMap().get(metricKey).getUnusedMap().get(VALUE).toString()).longValue());
            }
            if (INT.equals(type)) {
                writeMethod.invoke(obj,
                    Double.valueOf(eSBucket.getAggrMap().get(metricKey).getUnusedMap().get(VALUE).toString()).intValue());
            }
        }
    }

    /**
     * 利用反射获取属性
     * 注意：入参的clazz 中 timeStamp 放置在类属性的第一位置 来适配反射的应用
     * {
     *         "max_shardNu": {
     *           "max": {
     *             "field": "statis.writeTps"
     *           }
     *         },
     *         "max_unAssignedShards": {
     *           "max": {
     *             "field": "statis.readTps"
     *           }
     *         },
     *         ...
     *       }
     */
    <T> String buildAggsDSL(Class<T> clazz, String aggType) {
        StringBuilder sb = new StringBuilder();
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (TIME_STAMP.equals(fields[i].getName())) {
                continue;
            }

            Map<String, String> aggsSubSubCellMap = Maps.newHashMap();
            aggsSubSubCellMap.put(FIELD, STATIS + fields[i].getName());

            buildAggsDslMap(aggType, sb, fields[i].getName(), aggsSubSubCellMap);
            if (i != fields.length - 1) {
                sb.append(",").append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * example
     *
     * "os-cpu-percent":{"max":{"field":"metrics.os-cpu-percent"}},
     * "fs-total-disk_free_percent":{"max":{"field":"metrics.fs-total-disk_free_percent"}},
     * "os-cpu-load_average-1m":{"max":{"field":"metrics.os-cpu-load_average-1m"}},
     * "os-cpu-load_average-5m":{"max":{"field":"metrics.os-cpu-load_average-5m"}},
     * "os-cpu-load_average-15m":{"max":{"field":"metrics.os-cpu-load_average-15m"}},
     * "transport-tx_count_rate":{"max":{"field":"metrics.transport-tx_count_rate"}},
     *
     * @param metrics
     * @param aggType
     * @return
     */
    String buildAggsDSL(List<String> metrics, String aggType) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < metrics.size(); i++) {
            String metricName = metrics.get(i);
            Map<String, String> aggsSubSubCellMap = Maps.newHashMap();
            aggsSubSubCellMap.put(FIELD, METRICS + metricName);

            buildAggsDslMap(aggType, sb, metricName, aggsSubSubCellMap);
            if (i != metrics.size() - 1) {
                sb.append(",").append("\n");
            }
        }

        return sb.toString();
    }

    protected void buildAggsDslMap(String aggType, StringBuilder sb, String metricName,
                                 Map<String, String> aggsSubSubCellMap) {
        Map<String, Object> aggsSubCellMap = Maps.newHashMap();
        aggsSubCellMap.put(aggType, aggsSubSubCellMap);

        Map<String, Object> aggsCellMap = Maps.newHashMap();
        aggsCellMap.put(metricName, aggsSubCellMap);

        JSONObject jsonObject = new JSONObject(aggsCellMap);
        String str = jsonObject.toJSONString();
        sb.append(str, 1, str.length() - 1);
    }

    /**
     * 根据第一个时间点的值进行倒排，取topNu
     * 优化
     * todo: 通过es来做，先获取所有数据，去重，再排序
     */
    void mergeTopNuWithStep(VariousLineChartMetrics variousLineChartsMetrics, Integer topNu,String topMethod) {
        List<MetricsContent> sortedList = new ArrayList<>();
        switch (topMethod){
            case "avg":
                sortedList = variousLineChartsMetrics.getMetricsContents()
                        .stream()
                        .sorted(Comparator.comparing(x -> getAvgValue(x.getMetricsContentCells()), Comparator.reverseOrder()))
                        .limit(topNu != null ? topNu : 0)
                        .collect(Collectors.toList());
                break;
            case "max":
                sortedList = variousLineChartsMetrics.getMetricsContents()
                        .stream()
                        .sorted(Comparator.comparing(x -> getMaxValue(x.getMetricsContentCells()), Comparator.reverseOrder()))
                        .limit(topNu != null ? topNu : 0)
                        .collect(Collectors.toList());
                break;

        }

        //根据第一个时间点的值进行倒排，取topNu
        variousLineChartsMetrics.setMetricsContents(sortedList);
    }

    /**
     *  获取最大值
     * @param cells
     * @return
     */
    private double getMaxValue(List<MetricsContentCell> cells){
        double value = 0d;
        for (int i = 0; i < cells.size(); i++) {
            if (value<cells.get(i).getValue()){
                value = cells.get(i).getValue();
            }
        }
        return  value;
    }

    /**
     *  获取平均值
     *  todo:修改
     * @param cells
     * @return
     */
    private double getAvgValue(List<MetricsContentCell> cells){
        double value = 0d;
        for (int i = 0; i < cells.size(); i++) {
            value += cells.get(i).getValue();
        }
        return  value/cells.size();
    }

    /**
     * 根据第一个时间点的值进行倒排，取topNu
     */
    void mergeTopNu(VariousLineChartMetrics variousLineChartsMetrics, Integer topNu) {
        //根据第一个时间点的值进行倒排，取topNu
        List<MetricsContent> sortedList = variousLineChartsMetrics.getMetricsContents()
                .stream()
                .sorted(Comparator.comparing(x -> x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder()))
                .limit(topNu != null ? topNu : 0)
                .collect(Collectors.toList());

        variousLineChartsMetrics.setMetricsContents(sortedList);
    }

    /**
     * 获取单个ES实例/索引名称的指标信息
     * @param s                es响应体
     * @param metricsKeys      指标key
     * @param name             ES节点名称/索引名称/节点任务
     * @return
     */
    List<VariousLineChartMetrics> fetchSingleAggMetrics(ESQueryResponse s, List<String> metricsKeys, String name) {
        List<VariousLineChartMetrics> variousLineChartsMetrics = Lists.newArrayList();

        if (null == s || s.getAggs() == null) {
            LOGGER.warn("class=BaseAriusStatsESDAO||method=fetchSingleAggMetrics||msg=esQueryResponse is null");
            return variousLineChartsMetrics;
        }

        Map<String, ESAggr> esAggrMap = s.getAggs().getEsAggrMap();
        if (null != esAggrMap && null != esAggrMap.get(HIST)) {
            for (String metricsKey : metricsKeys) {

                try {
                    variousLineChartsMetrics.add(buildVariousLineChartMetricsForOneNode(metricsKey, name, esAggrMap));
                } catch (Exception e) {
                    LOGGER.error(
                        "class=BaseAriusStatsESDAO||method=fetchSingleAggMetrics||name={}||metricsKey={}||errMsg=exception! response:{}",
                        name, metricsKey, e);
                }
            }
        }

        return variousLineChartsMetrics;
    }
    
    /**
     *  此方法是用来处理非负型指标
     * @param
     * @param
     * @param topNu
     * @return
     */
     List<VariousLineChartMetrics> fetchMultipleNoNegativeAggMetrics(ESQueryResponse response,
        String oneLevelType, List<String> metricsKeys, Integer topNu) {
         List<VariousLineChartMetrics> variousLineChartsMetrics = Lists.newArrayList();
    
         if (null == response || response.getAggs() == null) {
             LOGGER.warn(
                 "class=BaseAriusStatsESDAO||method=fetchMultipleAggMetrics||msg=esQueryResponse is null");
             return variousLineChartsMetrics;
         }
         Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();
         if (null != esAggrMap && null != esAggrMap.get(HIST)) {
             metricsKeys.stream()
                 //对非负型指标进行获取
                 .map(key -> buildVariousLineNoNegativeChartMetrics(oneLevelType,key, esAggrMap))
                 //过滤除节点指标为空的状态
                 .filter(variousLineChartMetric -> CollectionUtils.isNotEmpty(
                     variousLineChartMetric.getMetricsContents()))
                 .forEach(variousLineChartsMetrics::add);
         }
    
         //get topNu
         if (topNu != null) {
             variousLineChartsMetrics.forEach(metrics -> mergeTopNu(metrics, topNu));
         }
        return variousLineChartsMetrics;
    }


    /**
     *
     * @param response         返回值
     * @param oneLevelType     一级指标类型(@link OneLevelTypeEnum)
     * @param metricsKeys      多个指标类型
     * @param topNu            topN
     * @return                 结果
     */
    List<VariousLineChartMetrics> fetchMultipleAggMetricsTop(ESQueryResponse response, String oneLevelType, List<String> metricsKeys,
                                                          Integer topNu,Integer topMethod) {
        List<VariousLineChartMetrics> variousLineChartsMetrics = Lists.newArrayList();

        if (null == response || response.getAggs() == null) {
            LOGGER.warn(
                    "class=BaseAriusStatsESDAO||method=fetchMultipleAggMetrics||msg=esQueryResponse is null");
            return variousLineChartsMetrics;
        }

        Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();
        if (null != esAggrMap && null != esAggrMap.get(HIST)) {
            metricsKeys.forEach(key -> variousLineChartsMetrics.add(buildVariousLineChartMetrics(oneLevelType ,key, esAggrMap)));
        }

        //get topNu
        if (topNu != null) {
            variousLineChartsMetrics.forEach(metrics -> mergeTopNu(metrics, topNu));
        }

        return variousLineChartsMetrics;
    }

    /**
     *
     * @param response         返回值
     * @param oneLevelType     一级指标类型(@link OneLevelTypeEnum)
     * @param metricsKeys      多个指标类型
     * @param topNu            topN
     * @return                 结果
     */
    List<VariousLineChartMetrics> fetchMultipleAggMetricsWithStep(ESQueryResponse response, String oneLevelType, List<String> metricsKeys,
                                                          Integer topNu,String topMethod) {
        List<VariousLineChartMetrics> variousLineChartsMetrics = Lists.newArrayList();

        if (null == response || response.getAggs() == null) {
            LOGGER.warn(
                    "class=BaseAriusStatsESDAO||method=fetchMultipleAggMetrics||msg=esQueryResponse is null");
            return variousLineChartsMetrics;
        }

        Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();
        if (null != esAggrMap && null != esAggrMap.get(HIST)) {
            metricsKeys.forEach(key -> variousLineChartsMetrics.add(buildVariousLineChartMetrics(oneLevelType ,key, esAggrMap)));
        }

        //get topNu
        if (topNu != null) {
            variousLineChartsMetrics.forEach(metrics -> mergeTopNuWithStep(metrics, topNu,topMethod));
        }

        return variousLineChartsMetrics;
    }

    /**
     *
     * @param response         返回值
     * @param oneLevelType     一级指标类型(@link OneLevelTypeEnum)
     * @param metricsKeys      多个指标类型
     * @param topNu            topN
     * @return                 结果
     */
    List<VariousLineChartMetrics> fetchMultipleAggMetrics(ESQueryResponse response, String oneLevelType, List<String> metricsKeys,
                                                          Integer topNu) {
        List<VariousLineChartMetrics> variousLineChartsMetrics = Lists.newArrayList();

        if (null == response || response.getAggs() == null) {
            LOGGER.warn(
                    "class=BaseAriusStatsESDAO||method=fetchMultipleAggMetrics||msg=esQueryResponse is null");
            return variousLineChartsMetrics;
        }

        Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();
        if (null != esAggrMap && null != esAggrMap.get(HIST)) {
            metricsKeys.forEach(key -> variousLineChartsMetrics.add(buildVariousLineChartMetrics(oneLevelType ,key, esAggrMap)));
        }

        //get topNu
        if (topNu != null) {
            variousLineChartsMetrics.forEach(metrics -> mergeTopNu(metrics, topNu));
        }

        return variousLineChartsMetrics;
    }


    TopMetrics buildTopMetrics(VariousLineChartMetrics variousLineChartMetrics) {
        TopMetrics topMetrics = new TopMetrics();
        topMetrics.setType(variousLineChartMetrics.getType());
        List<String> topNames = variousLineChartMetrics.getMetricsContents().stream().map(MetricsContent::getName)
                .collect(Collectors.toList());
        topMetrics.setTopNames(topNames);
        return topMetrics;
    }

    String buildTopNameStr(List<String> topName) {
        StringBuilder topNameStrSb = new StringBuilder();
        topNameStrSb.append("[");
        for (int i = 0; i < topName.size(); i++) {
            topNameStrSb.append("\"").append(topName.get(i)).append("\"");
            if (i == topName.size() - 1) {
                topNameStrSb.append("]");
            }else{
                topNameStrSb.append(",");
            }
        }
        return topNameStrSb.toString();
    }

    /**********************************************private*************************************************/

    private VariousLineChartMetrics buildVariousLineChartMetricsForOneNode(String key, String nodeName,
                                                                           Map<String, ESAggr> esAggrMap) {
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(key);
        variousLineChartMetrics.setMetricsContents(buildMetricsContentsForOneNode(key, nodeName, esAggrMap));
        return variousLineChartMetrics;
    }

    private List<MetricsContent> buildMetricsContentsForOneNode(String key, String nodeName,
                                                                Map<String, ESAggr> esAggrMap) {
        List<MetricsContent> metricsContents = Lists.newArrayList();
        MetricsContent metricsContent = new MetricsContent();
        metricsContent.setName(nodeName);

        List<MetricsContentCell> contentCells = Lists.newArrayList();
        esAggrMap.get(HIST).getBucketList().forEach(esBucket -> {
            if (null != esBucket.getUnusedMap().get(KEY)) {
                contentCells.add(buildMetricsContentCellsForOneNode(key, esBucket));
            }
        });

        metricsContent.setMetricsContentCells(contentCells);
        metricsContents.add(metricsContent);
        return metricsContents;

    }

    private MetricsContentCell buildMetricsContentCellsForOneNode(String key, ESBucket esBucket) {
        MetricsContentCell metricsContentCell = new MetricsContentCell();
        //get value
        ESAggr value = esBucket.getAggrMap().get(key);
        if (null != value && null != value.getUnusedMap().get(VALUE)) {
            metricsContentCell.setValue(Double.valueOf(value.getUnusedMap().get(VALUE).toString()));
        }

        //get time
        if (null != esBucket.getUnusedMap().get(KEY)) {
            metricsContentCell.setTimeStamp(Long.valueOf(esBucket.getUnusedMap().get(KEY).toString()));
        }

        return metricsContentCell;
    }

    private VariousLineChartMetrics buildVariousLineChartMetrics(String oneLevelType, String key, Map<String, ESAggr> esAggrMap) {
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(key);
        variousLineChartMetrics.setMetricsContents(buildMetricsContents(oneLevelType, key, esAggrMap));
        return variousLineChartMetrics;
    }
    private VariousLineChartMetrics buildVariousLineNoNegativeChartMetrics( String oneLevelType,String key,Map<String,
        ESAggr> esAggrMap){
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(key);
        variousLineChartMetrics.setMetricsContents(buildMetricsNoNegativeContents(oneLevelType,key,
            esAggrMap));
        return variousLineChartMetrics;
    }
    private List<MetricsContent> buildMetricsNoNegativeContents( String oneLevelType,String key, Map<String, ESAggr> esAggrMap) {
        List<MetricsContent> metricsContents = Lists.newArrayList();
    
        if (Objects.nonNull(esAggrMap.get(HIST))) {
            final List<ESBucket> bucketList = esAggrMap.get(HIST).getBucketList();
            if (CollectionUtils.isNotEmpty(bucketList)) {
                for (ESBucket esBucket : bucketList) {
                    //get nodeName
                    if (null != esBucket.getUnusedMap()) {
                        MetricsContent metricsContent = new MetricsContent();
                        if (Objects.nonNull(esBucket.getUnusedMap().get(KEY))) {
                            String keyValue = esBucket.getUnusedMap().get(KEY).toString();
                            if (null != oneLevelType && OneLevelTypeEnum.listNoClusterOneLevelType()
                                .contains(oneLevelType)) {
                                // 针对非集群维度指标，需要区分节点、模板、索引等所属集群
                                String[] keyArr = keyValue.split("@");
                                if (keyArr.length > 1) {
                                    String clusterName = keyArr[0];
                                    String name/*节点、模板、索引等名称*/ = keyArr[1];
                                    metricsContent.setCluster(clusterName);
                                    metricsContent.setName(name);
                                }
                            } else {
                                // 针对集群维度指标
                                metricsContent.setName(keyValue);
                                metricsContent.setCluster(keyValue);
                            }
                            final List<MetricsContentCell> metricsContentCells = buildMetricsNoNegativeContentCells(
                                key, esBucket);
                            //确定指标集合不为空
                            if (CollectionUtils.isNotEmpty(metricsContentCells)) {
                                metricsContent.setMetricsContentCells(metricsContentCells);
                                metricsContents.add(metricsContent);
                            }
                        }
                    
                    
                    }
                }
            }
        }
        return metricsContents;
    }

    private List<MetricsContent> buildMetricsContents(String oneLevelType, String key, Map<String, ESAggr> esAggrMap) {
        List<MetricsContent> metricsContents = Lists.newArrayList();

        esAggrMap.get(HIST).getBucketList().forEach(esBucket -> {
            //get nodeName
            if (null != esBucket.getUnusedMap().get(KEY)) {
                MetricsContent metricsContent = new MetricsContent();
                String keyValue = esBucket.getUnusedMap().get(KEY).toString();
                if (null != oneLevelType && OneLevelTypeEnum.listNoClusterOneLevelType().contains(oneLevelType)){
                    // 针对非集群维度指标，需要区分节点、模板、索引等所属集群
                    String[] keyArr = keyValue.split("@");
                    if (keyArr.length > 1) {
                        String clusterName                 = keyArr[0];
                        String name/*节点、模板、索引等名称*/  = keyArr[1];
                        metricsContent.setCluster(clusterName);
                        metricsContent.setName(name);
                    }
                }else {
                    // 针对集群维度指标
                    metricsContent.setName(keyValue);
                    metricsContent.setCluster(keyValue);
                }

                metricsContent.setMetricsContentCells(buildMetricsContentCells(key, esBucket));
                metricsContents.add(metricsContent);
            }
        });

        return metricsContents;
    }

    private List<MetricsContentCell> buildMetricsContentCells(String key, ESBucket esBucket) {
        List<MetricsContentCell> metricsContentCells = Lists.newArrayList();

        esBucket.getAggrMap().get(HIST).getBucketList().forEach(esSubBucket -> {
            MetricsContentCell metricsContentCell = new MetricsContentCell();

            // get timeStamp
            if (null != esSubBucket.getUnusedMap().get(KEY)) {
                metricsContentCell.setTimeStamp(Long.valueOf(esSubBucket.getUnusedMap().get(KEY).toString()));
            }

            //get value
            ESAggr esAggr = esSubBucket.getAggrMap().get(key);
            if (null != esAggr && null != esAggr.getUnusedMap().get(VALUE)) {
                metricsContentCell.setValue(Double.valueOf(esAggr.getUnusedMap().get(VALUE).toString()));
            }

            metricsContentCells.add(metricsContentCell);
        });

        return metricsContentCells;
    }
    private List<MetricsContentCell> buildMetricsNoNegativeContentCells(String key, ESBucket esBucket) {
        List<MetricsContentCell> metricsContentCells = Lists.newArrayList();
        
        esBucket.getAggrMap().get(HIST).getBucketList().forEach(esSubBucket -> {
            MetricsContentCell metricsContentCell = new MetricsContentCell();
            
            // get timeStamp
            final Object time = JSONPath.eval(esSubBucket.toJson(), String.format("$.%s", KEY));
            if (Objects.nonNull(time)) {
                metricsContentCell.setTimeStamp(Long.valueOf(String.valueOf(time)));
            }
         
            
            //get value
            final Object value = JSONPath.eval(esSubBucket.toJson(), String.format("$.%s.%s.%s", key,
                key,VALUE));
            /**
             * 由于采用了<em></>agg</em>中<em>filter</em>聚合模式，会导致到聚合字段的结果在获取不到的状态下是null状态，
             * 这里采用jsonpath{@link  JSONPath#eval(Object, String)}解析出来结果值，为了保证我们处理到的value不为空，所以将集合添加这一块一并加入判断
             * ，如果后续需要加入value值为空的，那么放开即可，但是同时需要对
             * {@link BaseAriusStatsESDAO#mergeTopNu(VariousLineChartMetrics, Integer)}中的<em>sorted</em>
             * 进行修改
             *   {
             *           "key" : "iNGVb_yJUrm",
             *           "doc_count" : 1,
             *           "hist" : {
             *             "buckets" : [
             *               {
             *                 "key_as_string" : "1648810200000",
             *                 "key" : 1648810200000,
             *                 "doc_count" : 1,
             *                 "gatewayFailedPer" : {
             *                   "doc_count" : 0,
             *                   "gatewayFailedPer" : {
             *                     "value" : null
             *                   }
             *                 }
             *               }
             *             ]
             *           }
             *         }，
             *          {
             *           "key" : "logi-elasticsearch-7.6.0",
             *           "doc_count" : 1,
             *           "hist" : {
             *             "buckets" : [
             *               {
             *                 "key_as_string" : "1648810200000",
             *                 "key" : 1648810200000,
             *                 "doc_count" : 1,
             *                 "gatewayFailedPer" : {
             *                   "doc_count" : 1,
             *                   "gatewayFailedPer" : {
             *                     "value" : 0.0
             *                   }
             *                 }
             *               }
             *             ]
             *           }
             *         }
             *
             */
            
            if (Objects.nonNull(value)) {
                metricsContentCell.setValue(Double.valueOf(String.valueOf(value)));
                metricsContentCells.add(metricsContentCell);
    
            }
            
        });
        
        return metricsContentCells;
    }

    /**
     * 构建分位值
     * @param percentiles2ValueMap
     * @param esAggMap
     */
    private void setPercentiles(Map<String, Double> percentiles2ValueMap, Map<String, ESAggr> esAggMap) {
        if (null != esAggMap.get(PERCENTILES) && null != esAggMap.get(PERCENTILES).getUnusedMap()
                && null != esAggMap.get(PERCENTILES).getUnusedMap().get(VALUES)) {
            JSONObject values = (JSONObject) esAggMap.get(PERCENTILES).getUnusedMap().get(VALUES);
            for (String percentilesType : PercentilesEnum.listAllType()) {
                if (null != values && null != values.getDouble(percentilesType)) {
                    percentiles2ValueMap.put(percentilesType, values.getDouble(percentilesType));
                }
            }
        }
    }

    /**
     * 构建task detail info
     * @param esQueryResponse
     * @return
     */
    protected List<ESClusterTaskDetail> buildTaskDetailInfo(ESQueryResponse esQueryResponse) {
        return esQueryResponse.getHits().getHits().stream().map(hit -> {
            ((JSONObject) hit.getSource()).getJSONObject("metrics");
            return ConvertUtil.obj2ObjByJSON(((JSONObject) hit.getSource()).getJSONObject("metrics"), ESClusterTaskDetail.class);
        }).collect(Collectors.toList());
    }

    /**
     * 获取有数据的第一个时间点
     * @param clusterPhyName
     * @param startTime
     * @param endTime
     * @param dslFormat
     * @return
     */
    protected Long getHasDataTime(String clusterPhyName, long startTime, long endTime, String dslFormat) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(dslFormat,
                clusterPhyName, startTime, endTime);
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequestWithRouting(metadataClusterName, null,
                realIndexName, TYPE, dsl, s -> s.getHits().getHits().size() > 0 ? ((Map<String, Long>)s.getHits().getHits().get(0).getSource()).get("timestamp") : null, 3);
    }
}