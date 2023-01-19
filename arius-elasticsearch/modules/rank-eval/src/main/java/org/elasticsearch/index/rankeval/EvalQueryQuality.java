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

package org.elasticsearch.index.rankeval;

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParserUtils;
import org.elasticsearch.index.rankeval.RatedDocument.DocumentKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Result of the evaluation metric calculation on one particular query alone.
 */
public class EvalQueryQuality implements ToXContentFragment, Writeable {

    private final String queryId;
    private final double metricScore;
    private MetricDetail optionalMetricDetails;
    private final List<RatedSearchHit> ratedHits;

    public EvalQueryQuality(String id, double metricScore) {
        this.queryId = id;
        this.metricScore = metricScore;
        this.ratedHits = new ArrayList<>();
    }

    public EvalQueryQuality(StreamInput in) throws IOException {
        this.queryId = in.readString();
        this.metricScore = in.readDouble();
        this.ratedHits = in.readList(RatedSearchHit::new);
        this.optionalMetricDetails = in.readOptionalNamedWriteable(MetricDetail.class);
    }

    // only used for parsing internally
    private EvalQueryQuality(String queryId, ParsedEvalQueryQuality builder) {
        this.queryId = queryId;
        this.metricScore = builder.evaluationResult;
        this.optionalMetricDetails = builder.optionalMetricDetails;
        this.ratedHits = builder.ratedHits;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(queryId);
        out.writeDouble(metricScore);
        out.writeList(ratedHits);
        out.writeOptionalNamedWriteable(this.optionalMetricDetails);
    }

    public String getId() {
        return queryId;
    }

    public double metricScore() {
        return metricScore;
    }

    public void setMetricDetails(MetricDetail breakdown) {
        this.optionalMetricDetails = breakdown;
    }

    public MetricDetail getMetricDetails() {
        return this.optionalMetricDetails;
    }

    public void addHitsAndRatings(List<RatedSearchHit> hits) {
        this.ratedHits.addAll(hits);
    }

    public List<RatedSearchHit> getHitsAndRatings() {
        return this.ratedHits;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(queryId);
        builder.field(METRIC_SCORE_FIELD.getPreferredName(), this.metricScore);
        builder.startArray(UNRATED_DOCS_FIELD.getPreferredName());
        for (DocumentKey key : EvaluationMetric.filterUnratedDocuments(ratedHits)) {
            builder.startObject();
            builder.field(RatedDocument.INDEX_FIELD.getPreferredName(), key.getIndex());
            builder.field(RatedDocument.DOC_ID_FIELD.getPreferredName(), key.getDocId());
            builder.endObject();
        }
        builder.endArray();
        builder.startArray(HITS_FIELD.getPreferredName());
        for (RatedSearchHit hit : ratedHits) {
            hit.toXContent(builder, params);
        }
        builder.endArray();
        if (optionalMetricDetails != null) {
            builder.field(METRIC_DETAILS_FIELD.getPreferredName(), optionalMetricDetails);
        }
        builder.endObject();
        return builder;
    }

    static final ParseField METRIC_SCORE_FIELD = new ParseField("metric_score");
    private static final ParseField UNRATED_DOCS_FIELD = new ParseField("unrated_docs");
    private static final ParseField HITS_FIELD = new ParseField("hits");
    private static final ParseField METRIC_DETAILS_FIELD = new ParseField("metric_details");
    private static final ObjectParser<ParsedEvalQueryQuality, Void> PARSER = new ObjectParser<>("eval_query_quality",
            true, ParsedEvalQueryQuality::new);

    private static class ParsedEvalQueryQuality {
        double evaluationResult;
        MetricDetail optionalMetricDetails;
        List<RatedSearchHit> ratedHits = new ArrayList<>();
    }

    static {
        PARSER.declareDouble((obj, value) -> obj.evaluationResult = value, METRIC_SCORE_FIELD);
        PARSER.declareObject((obj, value) -> obj.optionalMetricDetails = value, (p, c) -> parseMetricDetail(p),
                METRIC_DETAILS_FIELD);
        PARSER.declareObjectArray((obj, list) -> obj.ratedHits = list, (p, c) -> RatedSearchHit.parse(p), HITS_FIELD);
    }

    private static MetricDetail parseMetricDetail(XContentParser parser) throws IOException {
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser::getTokenLocation);
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.FIELD_NAME, parser.nextToken(), parser::getTokenLocation);
        MetricDetail metricDetail = parser.namedObject(MetricDetail.class, parser.currentName(), null);
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.END_OBJECT, parser.nextToken(), parser::getTokenLocation);
        return metricDetail;
    }

    public static EvalQueryQuality fromXContent(XContentParser parser, String queryId) throws IOException {
        return new EvalQueryQuality(queryId, PARSER.apply(parser, null));
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EvalQueryQuality other = (EvalQueryQuality) obj;
        return Objects.equals(queryId, other.queryId) &&
                Objects.equals(metricScore, other.metricScore) &&
                Objects.equals(ratedHits, other.ratedHits) &&
                Objects.equals(optionalMetricDetails, other.optionalMetricDetails);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(queryId, metricScore, ratedHits, optionalMetricDetails);
    }
}
