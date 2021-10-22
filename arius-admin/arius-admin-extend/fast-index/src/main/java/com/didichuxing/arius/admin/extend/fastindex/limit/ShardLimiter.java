package com.didichuxing.arius.admin.extend.fastindex.limit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShardLimiter {
    private Map<String, Set<Long>> currShards = new HashMap<>();


    public void add(String index, Long shard) {
        if(!currShards.containsKey(index)) {
            currShards.put(index, new HashSet<>());
        }

        currShards.get(index).add(shard);
    }

    public boolean tryOne(String index, Long shard) {
        if(currShards.containsKey(index) && currShards.get(index).contains(shard)) {
            return false;
        }
        return true;
    }

    public void clear() {
        currShards.clear();
    }
}
