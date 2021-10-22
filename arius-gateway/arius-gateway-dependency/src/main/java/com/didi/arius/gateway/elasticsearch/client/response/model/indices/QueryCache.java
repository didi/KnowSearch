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

package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class QueryCache {
    @JSONField(name = "memory_size_in_bytes")
    private long memorySizeInBytes;

    @JSONField(name = "total_count")
    private long totalCount;

    @JSONField(name = "hit_count")
    private long hitCount;

    @JSONField(name = "miss_count")
    private long missCount;

    @JSONField(name = "cache_size")
    private long cacheSize;

    @JSONField(name = "cache_count")
    private long cacheCount;

    @JSONField(name = "evictions")
    private long evictions;

    public long getMemorySizeInBytes() {
        return memorySizeInBytes;
    }

    public void setMemorySizeInBytes(long memorySizeInBytes) {
        this.memorySizeInBytes = memorySizeInBytes;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getHitCount() {
        return hitCount;
    }

    public void setHitCount(long hitCount) {
        this.hitCount = hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public void setMissCount(long missCount) {
        this.missCount = missCount;
    }

    public long getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
    }

    public long getCacheCount() {
        return cacheCount;
    }

    public void setCacheCount(long cacheCount) {
        this.cacheCount = cacheCount;
    }

    public long getEvictions() {
        return evictions;
    }

    public void setEvictions(long evictions) {
        this.evictions = evictions;
    }
}
