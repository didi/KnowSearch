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

package org.elasticsearch.client.transform.transforms;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.XContentParser;

import java.util.Objects;

import static org.elasticsearch.common.xcontent.ConstructingObjectParser.optionalConstructorArg;

public class TransformProgress {

    public static final ParseField TOTAL_DOCS = new ParseField("total_docs");
    public static final ParseField DOCS_REMAINING = new ParseField("docs_remaining");
    public static final ParseField PERCENT_COMPLETE = new ParseField("percent_complete");
    public static final ParseField DOCS_PROCESSED = new ParseField("docs_processed");
    public static final ParseField DOCS_INDEXED = new ParseField("docs_indexed");

    public static final ConstructingObjectParser<TransformProgress, Void> PARSER = new ConstructingObjectParser<>(
        "transform_progress",
        true,
        a -> new TransformProgress((Long) a[0], (Long)a[1], (Double)a[2], (Long)a[3], (Long)a[4]));

    static {
        PARSER.declareLong(optionalConstructorArg(), TOTAL_DOCS);
        PARSER.declareLong(optionalConstructorArg(), DOCS_REMAINING);
        PARSER.declareDouble(optionalConstructorArg(), PERCENT_COMPLETE);
        PARSER.declareLong(optionalConstructorArg(), DOCS_PROCESSED);
        PARSER.declareLong(optionalConstructorArg(), DOCS_INDEXED);
    }

    public static TransformProgress fromXContent(XContentParser parser) {
        return PARSER.apply(parser, null);
    }

    private final Long totalDocs;
    private final Long remainingDocs;
    private final Double percentComplete;
    private final long documentsProcessed;
    private final long documentsIndexed;

    public TransformProgress(Long totalDocs,
                             Long remainingDocs,
                             Double percentComplete,
                             Long documentsProcessed,
                             Long documentsIndexed) {
        this.totalDocs = totalDocs;
        this.remainingDocs = remainingDocs == null ? totalDocs : remainingDocs;
        this.percentComplete = percentComplete;
        this.documentsProcessed = documentsProcessed == null ? 0 : documentsProcessed;
        this.documentsIndexed = documentsIndexed == null ? 0 : documentsIndexed;
    }

    @Nullable
    public Double getPercentComplete() {
        return percentComplete;
    }

    @Nullable
    public Long getTotalDocs() {
        return totalDocs;
    }

    @Nullable
    public Long getRemainingDocs() {
        return remainingDocs;
    }

    public long getDocumentsProcessed() {
        return documentsProcessed;
    }

    public long getDocumentsIndexed() {
        return documentsIndexed;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (other == null || other.getClass() != getClass()) {
            return false;
        }

        TransformProgress that = (TransformProgress) other;
        return Objects.equals(this.remainingDocs, that.remainingDocs)
            && Objects.equals(this.totalDocs, that.totalDocs)
            && Objects.equals(this.percentComplete, that.percentComplete)
            && Objects.equals(this.documentsIndexed, that.documentsIndexed)
            && Objects.equals(this.documentsProcessed, that.documentsProcessed);
    }

    @Override
    public int hashCode(){
        return Objects.hash(remainingDocs, totalDocs, percentComplete, documentsIndexed, documentsProcessed);
    }
}
