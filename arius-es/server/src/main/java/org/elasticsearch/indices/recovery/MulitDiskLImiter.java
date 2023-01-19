package org.elasticsearch.indices.recovery;

import org.apache.lucene.store.RateLimiter;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardPath;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MulitDiskLImiter {
    private RateLimiter.SimpleRateLimiter globalLimiter;

    private Map<String, RateLimiter.SimpleRateLimiter> diskLimterMap = new ConcurrentHashMap<>();
    private double diskMbPerSec;

    public MulitDiskLImiter(double globalMbPerSec, double diskMbPerSec) {
        this.globalLimiter = new RateLimiter.SimpleRateLimiter(globalMbPerSec);
        this.diskMbPerSec = diskMbPerSec;
    }

    public void setGlobalMBPerSec(double globalMBPerSec) {
        this.globalLimiter.setMBPerSec(globalMBPerSec);
    }

    public void setDiskMBPerSec(double diskMBPerSec) {
        this.diskMbPerSec = diskMBPerSec;

        for (RateLimiter limiter : diskLimterMap.values()) {
            limiter.setMBPerSec(diskMbPerSec);
        }
    }

    public long getMinPauseCheckBytes() {
        return globalLimiter.getMinPauseCheckBytes();
    }

    public long pause(long bytes, IndexShard indexShard) throws IOException {
        long throttleTimeInNanos = globalLimiter.pause(bytes);

        String diskInfo = getDiskInfo(indexShard);
        if (!diskLimterMap.containsKey(diskInfo)) {
            synchronized (diskLimterMap) {
                if (!diskLimterMap.containsKey(diskInfo)) {
                    diskLimterMap.put(diskInfo, new RateLimiter.SimpleRateLimiter(diskMbPerSec));
                }
            }
        }

        RateLimiter diskLimiter = diskLimterMap.get(diskInfo);
        throttleTimeInNanos += diskLimiter.pause(bytes);

        return throttleTimeInNanos;
    }

    /* 获得shard对应的设备信息 */
    private String getDiskInfo(IndexShard indexShard) {
        ShardPath shardPath = indexShard.shardPath();
        return shardPath.getRootDataPath().toAbsolutePath().toString();
    }
}
