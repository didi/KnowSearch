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
package org.elasticsearch.client.ml.inference.trainedmodel.ensemble;


import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


public class WeightedMode implements OutputAggregator {

    public static final String NAME = "weighted_mode";
    public static final ParseField WEIGHTS = new ParseField("weights");
    public static final ParseField NUM_CLASSES = new ParseField("num_classes");

    @SuppressWarnings("unchecked")
    private static final ConstructingObjectParser<WeightedMode, Void> PARSER = new ConstructingObjectParser<>(
        NAME,
        true,
        a -> new WeightedMode((Integer)a[0], (List<Double>)a[1]));
    static {
        PARSER.declareInt(ConstructingObjectParser.constructorArg(), NUM_CLASSES);
        PARSER.declareDoubleArray(ConstructingObjectParser.optionalConstructorArg(), WEIGHTS);
    }

    public static WeightedMode fromXContent(XContentParser parser) {
        return PARSER.apply(parser, null);
    }

    private final List<Double> weights;
    private final int numClasses;

    public WeightedMode(int numClasses, List<Double> weights) {
        this.weights = weights;
        this.numClasses = numClasses;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject();
        if (weights != null) {
            builder.field(WEIGHTS.getPreferredName(), weights);
        }
        builder.field(NUM_CLASSES.getPreferredName(), numClasses);
        builder.endObject();
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeightedMode that = (WeightedMode) o;
        return Objects.equals(weights, that.weights) && numClasses == that.numClasses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(weights, numClasses);
    }
}
