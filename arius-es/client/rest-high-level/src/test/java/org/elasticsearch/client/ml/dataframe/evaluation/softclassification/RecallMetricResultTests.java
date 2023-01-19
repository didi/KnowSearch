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
package org.elasticsearch.client.ml.dataframe.evaluation.softclassification;

import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.AbstractXContentTestCase;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecallMetricResultTests extends AbstractXContentTestCase<RecallMetric.Result> {

    public static RecallMetric.Result randomResult() {
        return new RecallMetric.Result(
            Stream
                .generate(() -> randomDouble())
                .limit(randomIntBetween(1, 5))
                .collect(Collectors.toMap(v -> String.valueOf(randomDouble()), v -> v)));
    }

    @Override
    protected RecallMetric.Result createTestInstance() {
        return randomResult();
    }

    @Override
    protected RecallMetric.Result doParseInstance(XContentParser parser) throws IOException {
        return RecallMetric.Result.fromXContent(parser);
    }

    @Override
    protected boolean supportsUnknownFields() {
        return true;
    }

    @Override
    protected Predicate<String> getRandomFieldsExcludeFilter() {
        // disallow unknown fields in the root of the object as field names must be parsable as numbers
        return field -> field.isEmpty();
    }
}
