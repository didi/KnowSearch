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

/**
 * author weizijun
 * date：2019-07-25
 */
public class AvgCountMetricValue implements MetricValue {
    private long beforeTime;
    private long beforeValue;
    public final static long SECOND_COUNT = 1000;

    public long value(long metric, long now) {
        if (beforeTime == 0 || beforeTime >= now) {
            beforeTime = now;
            beforeValue = metric;
            return 0;
        }

        long value = (metric - beforeValue) / (now - beforeTime) * SECOND_COUNT;

        beforeTime = now;
        beforeValue = metric;

        return value;
    }
}
