package com.didi.arius.gateway.common.utils;

import com.didiglobal.knowframework.metrics.MetricsSource;
import com.didiglobal.knowframework.metrics.MetricsSystem;
import com.didiglobal.knowframework.metrics.lib.DefaultMetricsSystem;

public class MetricUtil {

	private MetricUtil(){}

	private static final String METRIC_PREFIX = "arius";

	private static MetricsSystem metricsSystem;

	/**
	 * 获取系统级别的指标系统
	 * 
	 * @return
	 */
	private static synchronized MetricsSystem getMetricsSystem() {
		if (null == metricsSystem) {
			metricsSystem = DefaultMetricsSystem.initialize(METRIC_PREFIX);
		}

		return metricsSystem;
	}

	/**
	 * 注册source
	 * 
	 * @param metricSetName
	 *            指标集名称
	 * @param desc
	 *            描述信息
	 * @param metricSource
	 *            指标数据源
	 */
	public static <T extends MetricsSource> T register(String metricSetName,
													   String desc, T metricSource) {
		return getMetricsSystem().register(metricSetName, desc, metricSource);
	}
}
