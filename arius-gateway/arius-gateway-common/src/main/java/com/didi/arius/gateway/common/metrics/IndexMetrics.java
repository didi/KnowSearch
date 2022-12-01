package com.didi.arius.gateway.common.metrics;

import com.didi.arius.gateway.common.utils.MetricUtil;
import com.didiglobal.knowframework.metrics.MetricsBuilder;
import com.didiglobal.knowframework.metrics.MetricsSource;
import com.didiglobal.knowframework.metrics.lib.MetricMutablePeriodGaugeLong;
import com.didiglobal.knowframework.metrics.lib.MetricMutableStat;
import com.didiglobal.knowframework.metrics.lib.MetricsRegistry;

/**
 * author weizijun
 * date：2019-08-28
 */
public class IndexMetrics implements MetricsSource {
    private MetricsRegistry metricsRegistry;

    private static final String INDEX_COUNT_NAME = "index.count";
    private static final String INDEX_COST_NAME = "index.cost";
    private static final String INDEX_REQUEST_AVG_LENGTH_NAME = "index.request.avg.length";
    private static final String INDEX_RESPONSE_AVG_LENGTH_NAME = "index.response.avg.length";

    private static final String INDEX_REQUEST_LENGTH_NAME = "index.request.length";
    private static final String INDEX_RESPONSE_LENGTH_NAME = "index.response.length";
    private static final String COUNT_NAME = "] count";

    private MetricMutablePeriodGaugeLong countMetric;

    private MetricMutableStat costMetric;

    private MetricMutableStat requestAvgLengthMetric;
    private MetricMutableStat responseAvgLengthMetric;

    private MetricMutablePeriodGaugeLong requestLengthMetric;
    private MetricMutablePeriodGaugeLong responseLengthMetric;

    public IndexMetrics(String index, String operation) {
        super();
        String name = "index_" + index + "_" + operation;
        metricsRegistry = new MetricsRegistry("index");
        metricsRegistry.tag("template", "", index);
        metricsRegistry.tag("operation", "", operation);

        countMetric = metricsRegistry.newPeriodGauge(INDEX_COUNT_NAME, "["
                + name + COUNT_NAME, 0L);

        costMetric = metricsRegistry.newStat(INDEX_COST_NAME,
                "["
                        + name + "] cost", "ops", "time", true);

        requestAvgLengthMetric = metricsRegistry.newStat(INDEX_REQUEST_AVG_LENGTH_NAME,
                "[" + name + "] request length", "ops", "length", true);

        responseAvgLengthMetric = metricsRegistry.newStat(INDEX_RESPONSE_AVG_LENGTH_NAME,
                "[" + name + "] response length", "ops", "length", true);

        requestLengthMetric = metricsRegistry.newPeriodGauge(INDEX_REQUEST_LENGTH_NAME, "["
                + name + COUNT_NAME, 0L);

        responseLengthMetric = metricsRegistry.newPeriodGauge(INDEX_RESPONSE_LENGTH_NAME, "["
                + name + COUNT_NAME, 0L);

        MetricUtil.register("gateway_"+name, "arius-gateway index metrics", this);
    }

    @Override
    public void getMetrics(MetricsBuilder builder, boolean all) {
        metricsRegistry.snapshot(builder.addRecord(metricsRegistry.name()),
                true);
    }

    public void incrCost(long cost) {
        countMetric.incr();
        costMetric.add(cost);
    }

    public void incrReqeustLength(long length) {
        requestLengthMetric.incr(length);
        requestAvgLengthMetric.add(length);
    }

    public void incrResponseLength(long length) {
        responseLengthMetric.incr(length);
        responseAvgLengthMetric.add(length);
    }
}
