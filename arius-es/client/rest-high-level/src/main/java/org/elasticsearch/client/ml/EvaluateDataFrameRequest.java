/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.client.ml;

import org.elasticsearch.client.Validatable;
import org.elasticsearch.client.ValidationException;
import org.elasticsearch.client.ml.dataframe.QueryConfig;
import org.elasticsearch.client.ml.dataframe.evaluation.Evaluation;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.elasticsearch.common.xcontent.ConstructingObjectParser.constructorArg;
import static org.elasticsearch.common.xcontent.ConstructingObjectParser.optionalConstructorArg;
import static org.elasticsearch.common.xcontent.XContentParserUtils.ensureExpectedToken;

public class EvaluateDataFrameRequest implements ToXContentObject, Validatable {

    private static final ParseField INDEX = new ParseField("index");
    private static final ParseField QUERY = new ParseField("query");
    private static final ParseField EVALUATION = new ParseField("evaluation");

    @SuppressWarnings("unchecked")
    private static final ConstructingObjectParser<EvaluateDataFrameRequest, Void> PARSER =
        new ConstructingObjectParser<>(
            "evaluate_data_frame_request",
            true,
            args -> new EvaluateDataFrameRequest((List<String>) args[0], (QueryConfig) args[1], (Evaluation) args[2]));

    static {
        PARSER.declareStringArray(constructorArg(), INDEX);
        PARSER.declareObject(optionalConstructorArg(), (p, c) -> QueryConfig.fromXContent(p), QUERY);
        PARSER.declareObject(constructorArg(), (p, c) -> parseEvaluation(p), EVALUATION);
    }

    private static Evaluation parseEvaluation(XContentParser parser) throws IOException {
        ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser::getTokenLocation);
        ensureExpectedToken(XContentParser.Token.FIELD_NAME, parser.nextToken(), parser::getTokenLocation);
        Evaluation evaluation = parser.namedObject(Evaluation.class, parser.currentName(), null);
        ensureExpectedToken(XContentParser.Token.END_OBJECT, parser.nextToken(), parser::getTokenLocation);
        return evaluation;
    }

    public static EvaluateDataFrameRequest fromXContent(XContentParser parser) {
        return PARSER.apply(parser, null);
    }

    private List<String> indices;
    private QueryConfig queryConfig;
    private Evaluation evaluation;

    public EvaluateDataFrameRequest(String index, @Nullable QueryConfig queryConfig, Evaluation evaluation) {
        this(Arrays.asList(index), queryConfig, evaluation);
    }

    public EvaluateDataFrameRequest(List<String> indices, @Nullable QueryConfig queryConfig, Evaluation evaluation) {
        setIndices(indices);
        setQueryConfig(queryConfig);
        setEvaluation(evaluation);
    }

    public List<String> getIndices() {
        return Collections.unmodifiableList(indices);
    }

    public final void setIndices(List<String> indices) {
        Objects.requireNonNull(indices);
        this.indices = new ArrayList<>(indices);
    }

    public QueryConfig getQueryConfig() {
        return queryConfig;
    }

    public final void setQueryConfig(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public final void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    @Override
    public Optional<ValidationException> validate() {
        List<String> errors = new ArrayList<>();
        if (indices.isEmpty()) {
            errors.add("At least one index must be specified");
        }
        if (evaluation == null) {
            errors.add("evaluation must not be null");
        }
        return errors.isEmpty()
            ? Optional.empty()
            : Optional.of(ValidationException.withErrors(errors));
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.array(INDEX.getPreferredName(), indices.toArray());
        if (queryConfig != null) {
            builder.field(QUERY.getPreferredName(), queryConfig.getQuery());
        }
        builder
            .startObject(EVALUATION.getPreferredName())
                .field(evaluation.getName(), evaluation)
            .endObject();
        builder.endObject();
        return builder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(indices, queryConfig, evaluation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluateDataFrameRequest that = (EvaluateDataFrameRequest) o;
        return Objects.equals(indices, that.indices)
            && Objects.equals(queryConfig, that.queryConfig)
            && Objects.equals(evaluation, that.evaluation);
    }
}
