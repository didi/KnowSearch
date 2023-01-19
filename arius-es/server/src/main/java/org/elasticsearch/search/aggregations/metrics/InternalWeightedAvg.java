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
package org.elasticsearch.search.aggregations.metrics;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InternalWeightedAvg extends InternalNumericMetricsAggregation.SingleValue implements WeightedAvg {
    private final double sum;
    private final double weight;

    InternalWeightedAvg(String name, double sum, double weight, DocValueFormat format, List<PipelineAggregator> pipelineAggregators,
                            Map<String, Object> metaData) {
        super(name, pipelineAggregators, metaData);
        this.sum = sum;
        this.weight = weight;
        this.format = format;
    }

    /**
     * Read from a stream.
     */
    public InternalWeightedAvg(StreamInput in) throws IOException {
        super(in);
        format = in.readNamedWriteable(DocValueFormat.class);
        sum = in.readDouble();
        weight = in.readDouble();
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeNamedWriteable(format);
        out.writeDouble(sum);
        out.writeDouble(weight);
    }

    @Override
    public double value() {
        return getValue();
    }

    @Override
    public double getValue() {
        return sum / weight;
    }

    double getSum() {
        return sum;
    }

    double getWeight() {
        return weight;
    }

    DocValueFormat getFormatter() {
        return format;
    }

    @Override
    public String getWriteableName() {
        return WeightedAvgAggregationBuilder.NAME;
    }

    @Override
    public InternalWeightedAvg reduce(List<InternalAggregation> aggregations, ReduceContext reduceContext) {
        CompensatedSum sumCompensation = new CompensatedSum(0, 0);
        CompensatedSum weightCompensation = new CompensatedSum(0, 0);

        // Compute the sum of double values with Kahan summation algorithm which is more
        // accurate than naive summation.
        for (InternalAggregation aggregation : aggregations) {
            InternalWeightedAvg avg = (InternalWeightedAvg) aggregation;
            weightCompensation.add(avg.weight);
            sumCompensation.add(avg.sum);
        }

        return new InternalWeightedAvg(getName(), sumCompensation.value(), weightCompensation.value(),
            format, pipelineAggregators(), getMetaData());
    }

    @Override
    public XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        builder.field(CommonFields.VALUE.getPreferredName(), weight != 0 ? getValue() : null);
        if (weight != 0 && format != DocValueFormat.RAW) {
            builder.field(CommonFields.VALUE_AS_STRING.getPreferredName(), format.format(getValue()));
        }
        return builder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sum, weight, format.getWriteableName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (super.equals(obj) == false) return false;
        InternalWeightedAvg other = (InternalWeightedAvg) obj;
        return Objects.equals(sum, other.sum) &&
                Objects.equals(weight, other.weight) &&
                Objects.equals(format.getWriteableName(), other.format.getWriteableName());
    }
}
