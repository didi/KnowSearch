package org.elasticsearch.index.cache.query;


import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.UsageTrackingQueryCachingPolicy;

/* 缓存term query */
public class CacheTermQueryCachePolicy extends UsageTrackingQueryCachingPolicy {

    public CacheTermQueryCachePolicy(int ringSize) {
        super(ringSize);
    }

    @Override
    protected boolean shouldNeverCache(Query query) {
        if (query instanceof TermQuery) {
            return false;
        }

        return super.shouldNeverCache(query);
    }
}
