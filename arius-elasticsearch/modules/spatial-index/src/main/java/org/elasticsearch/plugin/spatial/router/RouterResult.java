package org.elasticsearch.plugin.spatial.router;

import org.elasticsearch.plugin.spatial.config.IndexSpatialConfig;

import java.util.HashSet;
import java.util.Set;

public class RouterResult {
    private String indexName;

    private Set<Integer> shardIds;

    private Set<String> routings;

    public RouterResult(String indexName, Set<Integer> shardIds) {
        this.indexName = indexName;
        this.shardIds = shardIds;
    }

    public void toRoutings(IndexSpatialConfig config) {
        this.routings = new HashSet<>();
        for(int shardId : shardIds) {
            routings.add(config.toRouting(shardId));
        }
    }


    public String getRoutingStr() {
        StringBuilder sb = new StringBuilder();
        for (String routing: routings) {
            sb.append(routing).append(",");
        }

        return sb.substring(0, sb.length() - 1);
    }

    public String getShardIdsStr() {
        StringBuilder sb = new StringBuilder();
        for (int shardId : shardIds) {
            sb.append(shardId).append(",");
        }

        return sb.substring(0, sb.length() - 1);
    }

    public String getIndexName() {
        return indexName;
    }

    public Set<Integer> getShardIds() {
        return shardIds;
    }
}
