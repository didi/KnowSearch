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
package org.elasticsearch.client.graph;

import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.client.graph.GraphExploreRequest.TermBoost;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A request to identify terms from a choice of field as part of a {@link Hop}.
 * Optionally, a set of terms can be provided that are used as an exclusion or
 * inclusion list to filter which terms are considered.
 * 
 */
public class VertexRequest implements ToXContentObject {
    private String fieldName;
    private int size = DEFAULT_SIZE;
    public static final int DEFAULT_SIZE = 5;
    private Map<String, TermBoost> includes;
    private Set<String> excludes;
    public static final int DEFAULT_MIN_DOC_COUNT = 3;
    private int minDocCount = DEFAULT_MIN_DOC_COUNT;
    public static final int DEFAULT_SHARD_MIN_DOC_COUNT = 2;
    private int shardMinDocCount = DEFAULT_SHARD_MIN_DOC_COUNT;

   
    public VertexRequest() {

    }

    public String fieldName() {
        return fieldName;
    }

    public VertexRequest fieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public int size() {
        return size;
    }

    /**
     * @param size The maximum number of terms that should be returned from this field as part of this {@link Hop}
     */
    public VertexRequest size(int size) {
        this.size = size;
        return this;
    }

    public boolean hasIncludeClauses() {
        return includes != null && includes.size() > 0;
    }

    public boolean hasExcludeClauses() {
        return excludes != null && excludes.size() > 0;
    }

    /**
     * Adds a term that should be excluded from results
     * @param term A term to be excluded
     */
    public void addExclude(String term) {
        if (includes != null) {
            throw new IllegalArgumentException("Cannot have both include and exclude clauses");
        }
        if (excludes == null) {
            excludes = new HashSet<>();
        }
        excludes.add(term);
    }

    /**
     * Adds a term to the set of allowed values - the boost defines the relative
     * importance when pursuing connections in subsequent {@link Hop}s. The boost value
     * appears as part of the query. 
     * @param term a required term
     * @param boost an optional boost 
     */
    public void addInclude(String term, float boost) {
        if (excludes != null) {
            throw new IllegalArgumentException("Cannot have both include and exclude clauses");
        }
        if (includes == null) {
            includes = new HashMap<>();
        }
        includes.put(term, new TermBoost(term, boost));
    }

    public TermBoost[] includeValues() {
        return includes.values().toArray(new TermBoost[includes.size()]);
    }

    public String[] includeValuesAsStringArray() {
        String[] result = new String[includes.size()];
        int i = 0;
        for (TermBoost tb : includes.values()) {
            result[i++] = tb.term;
        }
        return result;
    }

    public String[] excludesAsArray() {
        return excludes.toArray(new String[excludes.size()]);
    }

    public int minDocCount() {
        return minDocCount;
    }

    /**
     * A "certainty" threshold which defines the weight-of-evidence required before
     * a term found in this field is identified as a useful connection
     * 
     * @param value The minimum number of documents that contain this term found in the samples used across all shards 
     */
    public VertexRequest minDocCount(int value) {
        minDocCount = value;
        return this;
    }


    public int shardMinDocCount() {
        return Math.min(shardMinDocCount, minDocCount);
    }

    /**
     * A "certainty" threshold which defines the weight-of-evidence required before
     * a term found in this field is identified as a useful connection
     * 
     * @param value The minimum number of documents that contain this term found in the samples used across all shards 
     */
    public VertexRequest shardMinDocCount(int value) {
        shardMinDocCount = value;
        return this;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field("field", fieldName);
        if (size != DEFAULT_SIZE) {
            builder.field("size", size);
        }
        if (minDocCount != DEFAULT_MIN_DOC_COUNT) {
            builder.field("min_doc_count", minDocCount);
        }
        if (shardMinDocCount != DEFAULT_SHARD_MIN_DOC_COUNT) {
            builder.field("shard_min_doc_count", shardMinDocCount);
        }
        if (includes != null) {
            builder.startArray("include");
            for (TermBoost tb : includes.values()) {
                builder.startObject();
                builder.field("term", tb.term);
                builder.field("boost", tb.boost);
                builder.endObject();
            }
            builder.endArray();
        }
        if (excludes != null) {
            builder.startArray("exclude");
            for (String value : excludes) {
                builder.value(value);
            }
            builder.endArray();
        }
        builder.endObject();
        return builder;
    }

}
