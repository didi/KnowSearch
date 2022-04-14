package com.didi.arius.gateway.elasticsearch.client.response.model.threadpool;

import com.alibaba.fastjson.annotation.JSONField;

public class ThreadPoolNodes {
       @JSONField(name = "bulk")
       private ThreadPoolNode bulk;

       @JSONField(name = "fetch_shard_started")
       private ThreadPoolNode fetchShardStarted;

       @JSONField(name = "fetch_shard_store")
       private ThreadPoolNode fetchShardStore;

       @JSONField(name = "flush")
       private ThreadPoolNode flush;

       @JSONField(name = "force_merge")
       private ThreadPoolNode forceMerge;

       @JSONField(name = "generic")
       private ThreadPoolNode generic;

       @JSONField(name = "get")
       private ThreadPoolNode get;

       @JSONField(name = "index")
       private ThreadPoolNode index;

       @JSONField(name = "listener")
       private ThreadPoolNode listener;

       @JSONField(name = "management")
       private ThreadPoolNode management;

       @JSONField(name = "percolate")
       private ThreadPoolNode percolate;

       @JSONField(name = "refresh")
       private ThreadPoolNode refresh;

       @JSONField(name = "search")
       private ThreadPoolNode search;

       @JSONField(name = "snapshot")
       private ThreadPoolNode snapshot;

       @JSONField(name = "suggest")
       private ThreadPoolNode suggest;

       @JSONField(name = "warmer")
       private ThreadPoolNode warmer;

       public ThreadPoolNodes() {
              // pass
       }

       public ThreadPoolNode getBulk() {
              return bulk;
       }

       public void setBulk(ThreadPoolNode bulk) {
              this.bulk = bulk;
       }

       public ThreadPoolNode getFetchShardStarted() {
              return fetchShardStarted;
       }

       public void setFetchShardStarted(ThreadPoolNode fetchShardStarted) {
              this.fetchShardStarted = fetchShardStarted;
       }

       public ThreadPoolNode getFetchShardStore() {
              return fetchShardStore;
       }

       public void setFetchShardStore(ThreadPoolNode fetchShardStore) {
              this.fetchShardStore = fetchShardStore;
       }

       public ThreadPoolNode getFlush() {
              return flush;
       }

       public void setFlush(ThreadPoolNode flush) {
              this.flush = flush;
       }

       public ThreadPoolNode getForceMerge() {
              return forceMerge;
       }

       public void setForceMerge(ThreadPoolNode forceMerge) {
              this.forceMerge = forceMerge;
       }

       public ThreadPoolNode getGeneric() {
              return generic;
       }

       public void setGeneric(ThreadPoolNode generic) {
              this.generic = generic;
       }

       public ThreadPoolNode getGet() {
              return get;
       }

       public void setGet(ThreadPoolNode get) {
              this.get = get;
       }

       public ThreadPoolNode getIndex() {
              return index;
       }

       public void setIndex(ThreadPoolNode index) {
              this.index = index;
       }

       public ThreadPoolNode getListener() {
              return listener;
       }

       public void setListener(ThreadPoolNode listener) {
              this.listener = listener;
       }

       public ThreadPoolNode getManagement() {
              return management;
       }

       public void setManagement(ThreadPoolNode management) {
              this.management = management;
       }

       public ThreadPoolNode getPercolate() {
              return percolate;
       }

       public void setPercolate(ThreadPoolNode percolate) {
              this.percolate = percolate;
       }

       public ThreadPoolNode getRefresh() {
              return refresh;
       }

       public void setRefresh(ThreadPoolNode refresh) {
              this.refresh = refresh;
       }

       public ThreadPoolNode getSearch() {
              return search;
       }

       public void setSearch(ThreadPoolNode search) {
              this.search = search;
       }

       public ThreadPoolNode getSnapshot() {
              return snapshot;
       }

       public void setSnapshot(ThreadPoolNode snapshot) {
              this.snapshot = snapshot;
       }

       public ThreadPoolNode getSuggest() {
              return suggest;
       }

       public void setSuggest(ThreadPoolNode suggest) {
              this.suggest = suggest;
       }

       public ThreadPoolNode getWarmer() {
              return warmer;
       }

       public void setWarmer(ThreadPoolNode warmer) {
              this.warmer = warmer;
       }
}
