package com.didi.arius.gateway.metrics.sink.mq;

import com.didi.arius.gateway.metrics.Metric;
import com.didi.arius.gateway.metrics.MetricsRecord;
import com.didi.arius.gateway.metrics.MetricsSink;
import com.didi.arius.gateway.metrics.MetricsTag;
import com.didi.arius.gateway.metrics.app.ApplicationInfoHolder;
import com.didi.arius.gateway.metrics.lib.MetricsRegistry;
import com.didi.arius.gateway.metrics.util.ProcessInfoUtils;
import com.didi.arius.gateway.metrics.util.TimeUtils;
import org.apache.commons.configuration.SubsetConfiguration;
import org.apache.commons.lang.StringUtils;

public abstract class AbstractMetricSink implements MetricsSink {

    private static final String SEPERATOR        = "|";

    private static final String METRIC_SEPARATOR = "=";

    @Override
    public void init(SubsetConfiguration conf) {
    }

    @Override
    public void putMetrics(MetricsRecord record) {
        Iterable<Metric> metrics = record.metrics();
        if (null == metrics || !metrics.iterator().hasNext()) {
            return;
        }

        String timeStr = TimeUtils.formatTimestamp(record.timestamp(), TimeUtils.TIME_YYYY_MM_DD_HH_MM_SS);
        String modeleName = ApplicationInfoHolder.getModelName();
        String nodeId = ApplicationInfoHolder.getNodeId() + ":" + ProcessInfoUtils.getProcessId();

        //retrieve the model name and node id from the tags
        StringBuilder tagBuilder = new StringBuilder();
        for (MetricsTag loopTag : record.tags()) {
            if (MetricsRegistry.MODEL_NAME.equals(loopTag.name())) {
                modeleName = StringUtils.trimToEmpty(loopTag.value());
            } else if (MetricsRegistry.NODE_ID.equals(loopTag.name())) {
                nodeId = StringUtils.trimToEmpty(loopTag.value());
            } else {
                tagBuilder.append(SEPERATOR).append(loopTag.name()).append(METRIC_SEPARATOR).append(loopTag.value());
            }
        }

        //build the record content
        // format of content like timestamp|modelname|nodeid|recordname|metrictag1=tagvalue1|...|metricname1=metricvalue1|...
        StringBuilder recordContent = new StringBuilder();
        recordContent.append(timeStr).append(SEPERATOR).append(modeleName).append(SEPERATOR).append(nodeId)
            .append(SEPERATOR).append(record.name()).append(tagBuilder).append(SEPERATOR);

        // one metric with one record
        //        for (Metric loopMetric : record.metrics()) {
        //            StringBuilder tmpSb = new StringBuilder(recordContent.toString());
        //            tmpSb.append(loopMetric.name()).append(METRIC_SEPARATOR).append(loopMetric.value()).append(SEPERATOR);
        //
        //            //remove the last separator
        //            tmpSb.setLength(tmpSb.length() - SEPERATOR.length());
        //            extendMessageProducerImpl.sendMsg(tmpSb.toString(), topic);
        //            if (LOGGER.isDebugEnabled()) {
        //                LOGGER.debug("success to send record " + tmpSb);
        //            }
        //        }

        //append all the metrics with one record
        for (Metric loopMetric : record.metrics()) {
            recordContent.append(loopMetric.name()).append(METRIC_SEPARATOR).append(loopMetric.value())
                .append(SEPERATOR);
        }

        //remove the last separator
        recordContent.setLength(recordContent.length() - SEPERATOR.length());
        sendMetrics(recordContent.toString());

    }

    @Override
    public void flush() {
    }

    public abstract void sendMetrics(String content);

}
