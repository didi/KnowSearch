package org.elasticsearch.plugin.spatial.config;

import org.elasticsearch.cluster.routing.Murmur3HashFunction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShardId2Routing {
    private int shardNum;
    private Map<Integer, String> shardId2Routing = new ConcurrentHashMap<>();

    public ShardId2Routing(int shardNum) {
        this.shardNum = shardNum;
    }

    public String toRouting(int shardId) {
        String routing = shardId2Routing.get(shardId);
        if (routing != null) {
            return routing;
        }

        int i = 0;
        String str;
        while (true) {
            i++;
            str = ""+i;

            if (shardId == toShardId(str, shardNum)) {
                break;
            }
        }

        shardId2Routing.put(shardId, str);
        return str;
    }


    private int toShardId(String routing, int shardNum) {
        final int hash = Murmur3HashFunction.hash(routing);
        return Math.floorMod(hash, shardNum);
    }
}
