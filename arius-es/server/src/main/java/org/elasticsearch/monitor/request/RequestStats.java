/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.monitor.request;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.metrics.MeanMetric;

/**
 * author weizijun
 * date：2019-07-25
 */
public class RequestStats {
    private static final Logger logger = LogManager.getLogger(RequestStats.class);

    private AvgMeanMetricValue ioTcpCostMetricValue = new AvgMeanMetricValue();
    private AvgMeanMetricValue ioHttpCostMetricValue = new AvgMeanMetricValue();
    private AvgMeanMetricValue searchCostMetricValue = new AvgMeanMetricValue();
    private AvgMeanMetricValue searchShardsCountMetricValue = new AvgMeanMetricValue();
    private AvgMeanMetricValue bulkCostMetricValue = new AvgMeanMetricValue();
    private AvgMeanMetricValue bulkShardsCountMetricValue = new AvgMeanMetricValue();

    public void stats(MeanMetric ioTcpCostMetric,
                      MeanMetric ioHttpCostMetric,
                      MeanMetric searchCostMetric,
                      MeanMetric searchShardsCountMetric,
                      MeanMetric bulkCostMetric,
                      MeanMetric bulkShardsCountMetric) {
        logger.info("RequestStats||ioTcpCost={}||ioHttpCost={}||searchCost={}||searchShardsCount={}||bulkCost={}||bulkShardsCount={}",
            ioTcpCostMetricValue.value(ioTcpCostMetric),
            ioHttpCostMetricValue.value(ioHttpCostMetric),
            searchCostMetricValue.value(searchCostMetric),
            searchShardsCountMetricValue.value(searchShardsCountMetric),
            bulkCostMetricValue.value(bulkCostMetric),
            bulkShardsCountMetricValue.value(bulkShardsCountMetric));
    }
}
