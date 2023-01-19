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

package org.elasticsearch.search.query;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.Version;
import org.elasticsearch.action.OriginalIndices;
import org.elasticsearch.common.io.stream.NamedWriteableAwareStreamInput;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.lucene.search.TopDocsAndMaxScore;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.SearchShardTarget;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.aggregations.InternalAggregationsTests;
import org.elasticsearch.search.aggregations.pipeline.SiblingPipelineAggregator;
import org.elasticsearch.search.suggest.SuggestTests;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.VersionUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static java.util.Collections.emptyList;

public class QuerySearchResultTests extends ESTestCase {

    private final NamedWriteableRegistry namedWriteableRegistry;

    public QuerySearchResultTests() {
        SearchModule searchModule = new SearchModule(Settings.EMPTY, false, emptyList());
        this.namedWriteableRegistry = new NamedWriteableRegistry(searchModule.getNamedWriteables());
    }

    private static QuerySearchResult createTestInstance() throws Exception {
        ShardId shardId = new ShardId("index", "uuid", randomInt());
        QuerySearchResult result = new QuerySearchResult(randomLong(), new SearchShardTarget("node", shardId, null, OriginalIndices.NONE));
        if (randomBoolean()) {
            result.terminatedEarly(randomBoolean());
        }
        TopDocs topDocs = new TopDocs(new TotalHits(randomLongBetween(0, Long.MAX_VALUE), TotalHits.Relation.EQUAL_TO), new ScoreDoc[0]);
        result.topDocs(new TopDocsAndMaxScore(topDocs, randomBoolean() ? Float.NaN : randomFloat()), new DocValueFormat[0]);
        result.size(randomInt());
        result.from(randomInt());
        if (randomBoolean()) {
            result.suggest(SuggestTests.createTestItem());
        }
        if (randomBoolean()) {
            result.aggregations(InternalAggregationsTests.createTestInstance());
        }
        return result;
    }

    public void testSerialization() throws Exception {
        QuerySearchResult querySearchResult = createTestInstance();
        Version version = VersionUtils.randomVersion(random());
        QuerySearchResult deserialized = copyWriteable(querySearchResult, namedWriteableRegistry, QuerySearchResult::new, version);
        assertEquals(querySearchResult.getRequestId(), deserialized.getRequestId());
        assertNull(deserialized.getSearchShardTarget());
        assertEquals(querySearchResult.topDocs().maxScore, deserialized.topDocs().maxScore, 0f);
        assertEquals(querySearchResult.topDocs().topDocs.totalHits, deserialized.topDocs().topDocs.totalHits);
        assertEquals(querySearchResult.from(), deserialized.from());
        assertEquals(querySearchResult.size(), deserialized.size());
        assertEquals(querySearchResult.hasAggs(), deserialized.hasAggs());
        if (deserialized.hasAggs()) {
            Aggregations aggs = querySearchResult.consumeAggs();
            Aggregations deserializedAggs = deserialized.consumeAggs();
            assertEquals(aggs.asList(), deserializedAggs.asList());
            List<SiblingPipelineAggregator> pipelineAggs = ((InternalAggregations) aggs).getTopLevelPipelineAggregators();
            List<SiblingPipelineAggregator> deserializedPipelineAggs =
                ((InternalAggregations) deserializedAggs).getTopLevelPipelineAggregators();
            assertEquals(pipelineAggs.size(), deserializedPipelineAggs.size());
            for (int i = 0; i < pipelineAggs.size(); i++) {
                SiblingPipelineAggregator pipelineAgg = pipelineAggs.get(i);
                SiblingPipelineAggregator deserializedPipelineAgg = deserializedPipelineAggs.get(i);
                assertArrayEquals(pipelineAgg.bucketsPaths(), deserializedPipelineAgg.bucketsPaths());
                assertEquals(pipelineAgg.name(), deserializedPipelineAgg.name());
            }
        }
        assertEquals(querySearchResult.terminatedEarly(), deserialized.terminatedEarly());
    }

    public void testReadFromPre_7_1_0() throws IOException {
        String message = "AAAAAAAAAGQAAAEAAAB/wAAAAAEBBnN0ZXJtcwVJblhNRgoDBVNhdWpvAAVrS3l3cwVHSVVZaAAFZXRUbEUFZGN0WVoABXhzYnVrAAEDAfoN" +
            "A3JhdwUBAAJRAAAAAAAAA30DBnN0ZXJtcwVNdVVFRwoAAAEDAfoNA3JhdwUBAAdDAAAAAAAAA30AAApQVkFhaUxSdHh5TAAAAAAAAAN9AAAKTVRUeUxnd1hyd" +
            "y0AAAAAAAADfQAACnZRQXZ3cWp0SmwPAAAAAAAAA30AAApmYXNyUUhNVWZBCwAAAAAAAAN9AAAKT3FIQ2RMZ1JZUwUAAAAAAAADfQAACm9jT05aZmZ4ZmUmAA" +
            "AAAAAAA30AAApvb0tJTkdvbHdzBnN0ZXJtcwVtRmlmZAoAAAEDAfoNA3JhdwUBAARXAAAAAAAAA30AAApZd3BwQlpBZEhpMQAAAAAAAAN9AAAKREZ3UVpTSXh" +
            "DSE4AAAAAAAADfQAAClVMZW1YZGtkSHUUAAAAAAAAA30AAApBUVdKVk1kTlF1BnN0ZXJtcwVxbkJGVgoAAAEDAfoNA3JhdwUBAAYJAAAAAAAAA30AAApBS2NL" +
            "U1ZVS25EIQAAAAAAAAN9AAAKWGpCbXZBZmduRhsAAAAAAAADfQAACk54TkJEV3pLRmI7AAAAAAAAA30AAApydkdaZnJycXhWSAAAAAAAAAN9AAAKSURVZ3JhQ" +
            "lFHSy4AAAAAAAADfQAACmJmZ0x5YlFlVksAClRJZHJlSkpVc1Y4AAAAAAAAA30DBnN0ZXJtcwVNdVVFRwoAAAEDAfoNA3JhdwUBAAdDAAAAAAAAA30AAApQVk" +
            "FhaUxSdHh5TAAAAAAAAAN9AAAKTVRUeUxnd1hydy0AAAAAAAADfQAACnZRQXZ3cWp0SmwPAAAAAAAAA30AAApmYXNyUUhNVWZBCwAAAAAAAAN9AAAKT3FIQ2R" +
            "MZ1JZUwUAAAAAAAADfQAACm9jT05aZmZ4ZmUmAAAAAAAAA30AAApvb0tJTkdvbHdzBnN0ZXJtcwVtRmlmZAoAAAEDAfoNA3JhdwUBAARXAAAAAAAAA30AAApZ" +
            "d3BwQlpBZEhpMQAAAAAAAAN9AAAKREZ3UVpTSXhDSE4AAAAAAAADfQAAClVMZW1YZGtkSHUUAAAAAAAAA30AAApBUVdKVk1kTlF1BnN0ZXJtcwVxbkJGVgoAA" +
            "AEDAfoNA3JhdwUBAAYJAAAAAAAAA30AAApBS2NLU1ZVS25EIQAAAAAAAAN9AAAKWGpCbXZBZmduRhsAAAAAAAADfQAACk54TkJEV3pLRmI7AAAAAAAAA30AAA" +
            "pydkdaZnJycXhWSAAAAAAAAAN9AAAKSURVZ3JhQlFHSy4AAAAAAAADfQAACmJmZ0x5YlFlVksACm5rdExLUHp3cGgBCm1heF9idWNrZXQFbmFtZTEBB2J1Y2t" +
            "ldDH/A3JhdwEBCm1heF9idWNrZXQFbmFtZTEBB2J1Y2tldDH/A3JhdwEAAAIAAf////8AAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        byte[] bytes = Base64.getDecoder().decode(message);
        try (NamedWriteableAwareStreamInput in = new NamedWriteableAwareStreamInput(StreamInput.wrap(bytes), namedWriteableRegistry)) {
            in.setVersion(Version.V_7_0_0);
            QuerySearchResult querySearchResult = new QuerySearchResult(in);
            assertEquals(100, querySearchResult.getRequestId());
            assertTrue(querySearchResult.hasAggs());
            InternalAggregations aggs = (InternalAggregations)querySearchResult.consumeAggs();
            assertEquals(1, aggs.asList().size());
            //top-level pipeline aggs are retrieved as part of InternalAggregations although they were serialized separately
            assertEquals(1, aggs.getTopLevelPipelineAggregators().size());
        }
    }
}
