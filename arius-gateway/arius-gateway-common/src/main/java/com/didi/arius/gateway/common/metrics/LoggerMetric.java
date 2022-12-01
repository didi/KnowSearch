package com.didi.arius.gateway.common.metrics;

import com.alibaba.fastjson.JSONObject;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.metrics.Metric;
import com.didiglobal.knowframework.metrics.MetricsRecord;
import com.didiglobal.knowframework.metrics.MetricsTag;
import com.didiglobal.knowframework.metrics.sink.mq.AbstractMetricSink;

public class LoggerMetric extends AbstractMetricSink {
	protected static final ILog logger = LogFactory.getLog("metrics");

	@Override
	public void putMetrics(MetricsRecord record) {
		Iterable<Metric> metrics = record.metrics();
		if (null == metrics || !metrics.iterator().hasNext()) {
			return;
		}

		JSONObject message = new JSONObject();
		message.put("timestamp", record.timestamp());
		for (MetricsTag loopTag : record.tags()) {
			message.put(loopTag.name().replace('.', '_'), loopTag.value());
		}

		message.put("type", record.name());

		//append all the metrics with one record
		for (Metric loopMetric : record.metrics()) {
			message.put(loopMetric.name().replace('.', '_'), loopMetric.value());
		}

		logger.info(message.toJSONString());
	}

	@Override
	public void sendMetrics(String content) {
		// pass
	}

}