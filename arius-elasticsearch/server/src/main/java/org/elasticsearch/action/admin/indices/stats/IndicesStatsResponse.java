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

package org.elasticsearch.action.admin.indices.stats;

import org.elasticsearch.action.admin.indices.stats.IndexStats.IndexStatsBuilder;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastResponse;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.Index;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableMap;

public class IndicesStatsResponse extends BroadcastResponse {

    private ShardStats[] shards;

    private Map<ShardRouting, ShardStats> shardStatsMap;

    IndicesStatsResponse(StreamInput in) throws IOException {
        super(in);
        shards = in.readArray(ShardStats::new, (size) -> new ShardStats[size]);
    }

    IndicesStatsResponse(ShardStats[] shards, int totalShards, int successfulShards, int failedShards,
                         List<DefaultShardOperationFailedException> shardFailures) {
        super(totalShards, successfulShards, failedShards, shardFailures);
        this.shards = shards;
    }

    public Map<ShardRouting, ShardStats> asMap() {
        if (this.shardStatsMap == null) {
            Map<ShardRouting, ShardStats> shardStatsMap = new HashMap<>();
            for (ShardStats ss : shards) {
                shardStatsMap.put(ss.getShardRouting(), ss);
            }
            this.shardStatsMap = unmodifiableMap(shardStatsMap);
        }
        return this.shardStatsMap;
    }

    public ShardStats[] getShards() {
        return this.shards;
    }

    public ShardStats getAt(int position) {
        return shards[position];
    }

    public IndexStats getIndex(String index) {
        return getIndices().get(index);
    }

    private Map<String, IndexStats> indicesStats;

    public Map<String, IndexStats> getIndices() {
        if (indicesStats != null) {
            return indicesStats;
        }

        final Map<String, IndexStatsBuilder> indexToIndexStatsBuilder = new HashMap<>();
        for (ShardStats shard : shards) {
            Index index = shard.getShardRouting().index();
            IndexStatsBuilder indexStatsBuilder = indexToIndexStatsBuilder.computeIfAbsent(index.getName(),
                    k -> new IndexStatsBuilder(k, index.getUUID()));
            indexStatsBuilder.add(shard);
        }

        indicesStats = indexToIndexStatsBuilder.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().build()));
        return indicesStats;
    }

    private CommonStats total = null;

    public CommonStats getTotal() {
        if (total != null) {
            return total;
        }
        CommonStats stats = new CommonStats();
        for (ShardStats shard : shards) {
            stats.add(shard.getStats());
        }
        total = stats;
        return stats;
    }

    private CommonStats primary = null;

    public CommonStats getPrimaries() {
        if (primary != null) {
            return primary;
        }
        CommonStats stats = new CommonStats();
        for (ShardStats shard : shards) {
            if (shard.getShardRouting().primary()) {
                stats.add(shard.getStats());
            }
        }
        primary = stats;
        return stats;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeArray(shards);
    }

    @Override
    protected void addCustomXContentFields(XContentBuilder builder, Params params) throws IOException {
        final String level = params.param("level", "indices");
        final boolean isLevelValid =
            "cluster".equalsIgnoreCase(level) || "indices".equalsIgnoreCase(level) || "shards".equalsIgnoreCase(level);
        if (!isLevelValid) {
            throw new IllegalArgumentException("level parameter must be one of [cluster] or [indices] or [shards] but was [" + level + "]");
        }

        builder.startObject("_all");

        builder.startObject("primaries");
        getPrimaries().toXContent(builder, params);
        builder.endObject();

        builder.startObject("total");
        getTotal().toXContent(builder, params);
        builder.endObject();

        builder.endObject();

        if ("indices".equalsIgnoreCase(level) || "shards".equalsIgnoreCase(level)) {
            builder.startObject(Fields.INDICES);
            for (IndexStats indexStats : getIndices().values()) {
                builder.startObject(indexStats.getIndex());
                builder.field("uuid", indexStats.getUuid());
                builder.startObject("primaries");
                indexStats.getPrimaries().toXContent(builder, params);
                builder.endObject();

                builder.startObject("total");
                indexStats.getTotal().toXContent(builder, params);
                builder.endObject();

                if ("shards".equalsIgnoreCase(level)) {
                    builder.startObject(Fields.SHARDS);
                    for (IndexShardStats indexShardStats : indexStats) {
                        builder.startArray(Integer.toString(indexShardStats.getShardId().id()));
                        for (ShardStats shardStats : indexShardStats) {
                            builder.startObject();
                            shardStats.toXContent(builder, params);
                            builder.endObject();
                        }
                        builder.endArray();
                    }
                    builder.endObject();
                }
                builder.endObject();
            }
            builder.endObject();
        }
    }

    static final class Fields {
        static final String INDICES = "indices";
        static final String SHARDS = "shards";
    }

    @Override
    public String toString() {
        return Strings.toString(this, true, false);
    }
}
