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


package org.elasticsearch.search.aggregations.bucket.significant.heuristics;


import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.ConstructingObjectParser.optionalConstructorArg;

public class GND extends NXYSignificanceHeuristic {
    public static final String NAME = "gnd";
    public static final ConstructingObjectParser<GND, Void> PARSER = new ConstructingObjectParser<>(NAME, args -> {
        boolean backgroundIsSuperset = args[0] == null ? true : (boolean) args[0];
        return new GND(backgroundIsSuperset);
    });
    static {
        PARSER.declareBoolean(optionalConstructorArg(), BACKGROUND_IS_SUPERSET);
    }

    public GND(boolean backgroundIsSuperset) {
        super(true, backgroundIsSuperset);
    }

    /**
     * Read from a stream.
     */
    public GND(StreamInput in) throws IOException {
        super(true, in.readBoolean());
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeBoolean(backgroundIsSuperset);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GND)) {
            return false;
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        int result = NAME.hashCode();
        result = 31 * result + super.hashCode();
        return result;
    }

    /**
     * Calculates Google Normalized Distance, as described in "The Google Similarity Distance", Cilibrasi and Vitanyi, 2007
     * link: http://arxiv.org/pdf/cs/0412098v3.pdf
     */
    @Override
    public double getScore(long subsetFreq, long subsetSize, long supersetFreq, long supersetSize) {

        Frequencies frequencies = computeNxys(subsetFreq, subsetSize, supersetFreq, supersetSize, "GND");
        double fx = frequencies.N1_;
        double fy = frequencies.N_1;
        double fxy = frequencies.N11;
        double N = frequencies.N;
        if (fxy == 0) {
            // no co-occurrence
            return 0.0;
        }
        if ((fx == fy) && (fx == fxy)) {
            // perfect co-occurrence
            return 1.0;
        }
        double score = (Math.max(Math.log(fx), Math.log(fy)) - Math.log(fxy)) /
                (Math.log(N) - Math.min(Math.log(fx), Math.log(fy)));

        //we must invert the order of terms because GND scores relevant terms low
        score = Math.exp(-1.0d * score);
        return score;
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.field(BACKGROUND_IS_SUPERSET.getPreferredName(), backgroundIsSuperset);
        builder.endObject();
        return builder;
    }

    public static class GNDBuilder extends NXYBuilder {
        public GNDBuilder(boolean backgroundIsSuperset) {
            super(true, backgroundIsSuperset);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject(NAME);
            builder.field(BACKGROUND_IS_SUPERSET.getPreferredName(), backgroundIsSuperset);
            builder.endObject();
            return builder;
        }
    }
}

