package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob;

import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.metrics.CollectMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESDataTempBean;
import com.google.common.collect.Lists;

import java.util.concurrent.CopyOnWriteArrayList;

import static com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.metrics.MetricsComputeType.*;


public class MonitorCollectMetrics {
    /**
     * 采集线程会顺序执行以下指标采集,所有复合计算的最好放最后,这样不会导致因为复合计算需要的参数没有算出来导致失败
     */
    public static CopyOnWriteArrayList<CollectMetrics> initIndexDataRegisterMap() {
        CopyOnWriteArrayList<CollectMetrics> indexWorkOrders = Lists.newCopyOnWriteArrayList();

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.indexing.index_total")
                .valueRoute("indexing.index_total")
                .computeType(NONE).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.indexing.index_total_rate")
                .valueRoute("indexing.index_total")
                .computeType(AVG).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.indexing.index_time_in_millis_rate")
                .valueRoute("indexing.index_time_in_millis")
                .computeType(AVG).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.merges.current")
                .valueRoute("merges.current")
                .computeType(NONE).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.merges.total")
                .valueRoute("merges.total")
                .computeType(MINUS).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.merges.total_time_in_millis")
                .valueRoute("merges.total_time_in_millis")
                .computeType(MINUS).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.segments.count")
                .valueRoute("segments.count")
                .computeType(NONE).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.search.query_total_rate")
                .valueRoute("search.query_total")
                .computeType(AVG).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.search.fetch_total_rate")
                .valueRoute("search.fetch_total")
                .computeType(AVG).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.search.scroll_total_rate")
                .valueRoute("search.scroll_total")
                .computeType(AVG).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.search.query_time_in_millis_rate")
                .valueRoute("search.query_time_in_millis")
                .computeType(AVG).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.search.fetch_time_in_millis_rate")
                .valueRoute("search.fetch_time_in_millis")
                .computeType(AVG).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.search.scroll_time_in_millis_rate")
                .valueRoute("search.scroll_time_in_millis")
                .computeType(AVG).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.refresh.total")
                .valueRoute("refresh.total")
                .computeType(MINUS).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.refresh.total_time_in_millis")
                .valueRoute("refresh.total_time_in_millis")
                .computeType(MINUS).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.flush.total")
                .valueRoute("flush.total")
                .computeType(MINUS).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.flush.total_time_in_millis")
                .valueRoute("flush.total_time_in_millis")
                .computeType(MINUS).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.indexing.index_failed_rate")
                .valueRoute("indexing.index_failed")
                .computeType(AVG).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.store.size_in_bytes")
                .valueRoute("store.size_in_bytes")
                .computeType(NONE).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.store.size_in_bytes_rate")
                .valueRoute("store.size_in_bytes")
                .computeType(AVG).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.docs.count")
                .valueRoute("docs.count")
                .computeType(NONE).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.segments.memory_in_bytes")
                .valueRoute("segments.memory_in_bytes")
                .computeType(NONE).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.indices.indexing.index_latency")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.indices.indexing.index_time_in_millis_rate")
                .deriveParam( ESDataTempBean.DIVISOR, "es.indices.indexing.index_total_rate")
                .computeType(DERIVE_DIVISION).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.indices.search.query_avg_time")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.indices.search.query_time_in_millis_rate")
                .deriveParam( ESDataTempBean.DIVISOR, "es.indices.search.query_total_rate")
                .computeType(DERIVE_DIVISION).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.indices.search.fetch_avg_time")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.indices.search.fetch_time_in_millis_rate")
                .deriveParam( ESDataTempBean.DIVISOR, "es.indices.search.fetch_total_rate")
                .computeType(DERIVE_DIVISION).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.indices.search.scroll_avg_time")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.indices.search.scroll_time_in_millis_rate")
                .deriveParam( ESDataTempBean.DIVISOR, "es.indices.search.scroll_total_rate")
                .computeType(DERIVE_DIVISION).build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.indices.refresh_avg_time")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.indices.refresh.total_time_in_millis")
                .deriveParam( ESDataTempBean.DIVISOR, "es.indices.refresh.total").computeType(DERIVE_DIVISION)
                .build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.indices.flush_avg_time")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.indices.flush.total_time_in_millis")
                .deriveParam( ESDataTempBean.DIVISOR, "es.indices.flush.total").computeType(DERIVE_DIVISION)
                .build());

        indexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.indices.merge_avg_time")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.indices.merges.total_time_in_millis")
                .deriveParam( ESDataTempBean.DIVISOR, "es.indices.merges.total").computeType(DERIVE_DIVISION)
                .build());

        return indexWorkOrders;
    }

    /**
     * 采集线程会顺序执行以下指标采集,所有复合计算的最好放最后,这样不会导致因为复合计算需要的参数没有算出来导致失败
     */
    public static CopyOnWriteArrayList<CollectMetrics> initNodeDataRegisterMap() {
        CopyOnWriteArrayList<CollectMetrics> nodeWorkOrders = Lists.newCopyOnWriteArrayList();

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.merges.current")
                .valueRoute("indices.merges.current")
                .computeType(NONE).sendToOdin().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.merges.total")
                .valueRoute("indices.merges.total")
                .computeType(MINUS).bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.merges.total_time_in_millis")
                .valueRoute("indices.merges.total_time_in_millis")
                .computeType(MINUS).bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.refresh.total")
                .valueRoute("indices.refresh.total ")
                .computeType(MINUS).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.refresh.total_time_in_millis")
                .valueRoute("indices.refresh.total_time_in_millis")
                .computeType(MINUS).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.flush.total ")
                .valueRoute("indices.flush.total ")
                .computeType(MINUS).sendToOdin().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.flush.total_time_in_millis ")
                .valueRoute("indices.flush.total_time_in_millis ")
                .computeType(MINUS).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.segments.count")
                .valueRoute("indices.segments.count")
                .computeType(NONE).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.segments.memory_in_bytes")
                .valueRoute("indices.segments.memory_in_bytes")
                .computeType(NONE).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.jvm.mem.heap_used_percent")
                .valueRoute("jvm.mem.heap_used_percent")
                .computeType(NONE).bIndexToNodeMetrics().sendToOdin().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.os.cpu_percent")
                .valueRoute("os.cpu_percent")
                .computeType(NONE).needCorrectFromOdin().bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.jvm.gc.old.collection_count")
                .valueRoute("jvm.gc.collectors.old.collection_count")
                .computeType(MINUS).bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.jvm.gc.old.collection_time_in_millis")
                .valueRoute("jvm.gc.collectors.old.collection_time_in_millis")
                .computeType(MINUS).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.thread_pool.bulk.rejected")
                .valueRoute("thread_pool.bulk.rejected")
                .computeType(MINUS).bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.thread_pool.bulk.completed")
                .valueRoute("thread_pool.bulk.completed")
                .computeType(MINUS).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.thread_pool.bulk.queue")
                .valueRoute("thread_pool.bulk.queue")
                .computeType(NONE).bIndexToNodeMetrics().sendToOdin().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.thread_pool.search.rejected")
                .valueRoute("thread_pool.search.rejected")
                .computeType(MINUS).sendToOdin().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.thread_pool.search.completed")
                .valueRoute("thread_pool.search.completed")
                .computeType(MINUS).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.thread_pool.search.queue")
                .valueRoute("thread_pool.search.queue")
                .computeType(NONE).sendToOdin().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.transport.rx_count_rate")
                .valueRoute("transport.rx_count")
                .computeType(AVG).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.transport.rx_size_in_bytes_rate")
                .valueRoute("transport.rx_size_in_bytes")
                .computeType(AVG).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.transport.tx_count_rate")
                .valueRoute("transport.tx_count")
                .computeType(AVG).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.transport.tx_size_in_bytes_rate")
                .valueRoute("transport.tx_size_in_bytes")
                .computeType(AVG).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.fs.total.disk_free_percent")
                .valueRoute("fs.total.free_in_bytes/fs.total.total_in_bytes")
                .computeType(NONE).bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.docs.count")
                .valueRoute("indices.docs.count")
                .computeType(NONE).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.store.size_in_bytes")
                .valueRoute("indices.store.size_in_bytes")
                .computeType(NONE).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.indexing.index_total_rate")
                .valueRoute("indices.indexing.index_total")
                .computeType(AVG).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.indexing.index_time_in_millis_rate")
                .valueRoute("indices.indexing.index_time_in_millis")
                .computeType(AVG).bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.indexing.delete_total_rate")
                .valueRoute("indices.indexing.delete_total")
                .computeType(AVG).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.search.query_total_rate")
                .valueRoute("indices.search.query_total")
                .computeType(AVG).bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.search.query_time_in_millis_rate")
                .valueRoute("indices.search.query_time_in_millis")
                .computeType(AVG).bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.search.fetch_total_rate")
                .valueRoute("indices.search.fetch_total")
                .computeType(AVG).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.search.fetch_time_in_millis_rate")
                .valueRoute("indices.search.fetch_time_in_millis")
                .computeType(AVG).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.search.scroll_total_rate")
                .valueRoute("indices.search.scroll_total")
                .computeType(AVG).bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.search.scroll_time_in_millis_rate")
                .valueRoute("indices.search.scroll_time_in_millis")
                .computeType(AVG).bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.process.cpu.percent")
                .valueRoute("process.cpu.percent")
                .computeType(NONE).bIndexToNodeMetrics().build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.process.process.open_file_descriptors")
                .valueRoute("process.open_file_descriptors")
                .computeType(NONE).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.jvm.threads.count")
                .valueRoute("jvm.threads.count")
                .computeType(NONE).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.transport.server_open")
                .valueRoute("transport.server_open")
                .computeType(NONE).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.translog.operations_rate")
                .valueRoute("indices.translog.operations")
                .computeType(AVG).build());

        //ingest
        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.ingest.total.count")
                .valueRoute("ingest.total.count")
                .computeType(AVG).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.ingest.total.time_in_millis")
                .valueRoute("ingest.total.time_in_millis")
                .computeType(AVG).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.ingest.total.failed")
                .valueRoute("ingest.total.failed")
                .computeType(AVG).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.ingest.total.current")
                .valueRoute("ingest.total.current")
                .computeType(NONE).build());

        //fs
        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.fs.total.total_in_bytes")
                .valueRoute("fs.total.total_in_bytes")
                .computeType(NONE).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.fs.total.free_in_bytes")
                .valueRoute("fs.total.free_in_bytes")
                .computeType(NONE).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.fs.total.available_in_bytes")
                .valueRoute("fs.total.available_in_bytes")
                .computeType(NONE).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.merges_avg_time")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.node.indices.merges.total_time_in_millis")
                .deriveParam( ESDataTempBean.DIVISOR, "es.node.indices.merges.total")
                .computeType(DERIVE_DIVISION).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.refresh_avg_time")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.node.indices.refresh.total_time_in_millis")
                .deriveParam( ESDataTempBean.DIVISOR, "es.node.indices.refresh.total")
                .computeType(DERIVE_DIVISION).build());

        nodeWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.indices.flush_avg_time")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.node.indices.flush.total_time_in_millis")
                .deriveParam( ESDataTempBean.DIVISOR, "es.node.indices.flush.total")
                .computeType(DERIVE_DIVISION).build());

        return nodeWorkOrders;
    }

    public static CopyOnWriteArrayList<CollectMetrics> initNodeToIndexDataRegisterMap() {
        CopyOnWriteArrayList<CollectMetrics> nodeToIndexWorkOrders = Lists.newCopyOnWriteArrayList();

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.merges.current")
                .valueRoute("merges.current")
                .computeType(NONE).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.merges.total")
                .valueRoute("merges.total")
                .computeType(MINUS).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.merges.total_time_in_millis")
                .valueRoute("merges.total_time_in_millis")
                .computeType(MINUS).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.refresh.total")
                .valueRoute("refresh.total ")
                .computeType(MINUS).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.refresh.total_time_in_millis")
                .valueRoute("refresh.total_time_in_millis")
                .computeType(MINUS).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.flush.total ")
                .valueRoute("flush.total ")
                .computeType(MINUS).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.flush.total_time_in_millis ")
                .valueRoute("flush.total_time_in_millis ")
                .computeType(MINUS).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.segments.count")
                .valueRoute("segments.count")
                .computeType(NONE).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.segments.memory_in_bytes")
                .valueRoute("segments.memory_in_bytes")
                .computeType(NONE).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.docs.count")
                .valueRoute("docs.count")
                .computeType(NONE).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.store.size_in_bytes")
                .valueRoute("store.size_in_bytes")
                .computeType(NONE).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.indexing.index_total_rate")
                .valueRoute("indexing.index_total")
                .computeType(AVG).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.indexing.index_time_in_millis_rate")
                .valueRoute("indexing.index_time_in_millis")
                .computeType(AVG).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.search.query_total_rate")
                .valueRoute("search.query_total")
                .computeType(AVG).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.search.fetch_total_rate")
                .valueRoute("search.fetch_total")
                .computeType(AVG).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.search.scroll_total_rate")
                .valueRoute("search.scroll_total")
                .computeType(AVG).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.search.query_time_in_millis_rate")
                .valueRoute("search.query_time_in_millis")
                .computeType(AVG).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.search.fetch_time_in_millis_rate")
                .valueRoute("search.fetch_time_in_millis")
                .computeType(AVG).build());

        nodeToIndexWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.index.search.scroll_time_in_millis_rate")
                .valueRoute("search.scroll_time_in_millis")
                .computeType(AVG).build());

        return nodeToIndexWorkOrders;
    }

    public static CopyOnWriteArrayList<CollectMetrics> initIndexToNodeDataRegisterMap(CopyOnWriteArrayList<CollectMetrics> nodeWorkOrders) {
        CopyOnWriteArrayList<CollectMetrics> indexToNodeWorkOrders = Lists.newCopyOnWriteArrayList();

        for (CollectMetrics workOrder : nodeWorkOrders) {
            if (workOrder.isIndexToNodeMetrics()) {
                indexToNodeWorkOrders.add(workOrder);
            }
        }

        return indexToNodeWorkOrders;
    }

    public static CopyOnWriteArrayList<CollectMetrics> initIngestDataRegisterMap() {
        CopyOnWriteArrayList<CollectMetrics> ingestWorkOrders = Lists.newCopyOnWriteArrayList();

        ingestWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.ingest.count")
                .valueRoute("count")
                .computeType(AVG).bIndexToNodeMetrics().build());

        ingestWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.ingest.time_in_millis")
                .valueRoute("time_in_millis")
                .computeType(AVG).bIndexToNodeMetrics().build());

        ingestWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.ingest.current")
                .valueRoute("current")
                .computeType(NONE).bIndexToNodeMetrics().build());

        ingestWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.node.ingest.failed")
                .valueRoute("failed")
                .computeType(AVG).bIndexToNodeMetrics().build());

        return ingestWorkOrders;
    }

    public static CopyOnWriteArrayList<CollectMetrics> initDCDRDataRegisterMap() {
        CopyOnWriteArrayList<CollectMetrics> dcdrWorkOrders = Lists.newCopyOnWriteArrayList();

        // max_seq_no延迟量
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.max_seq_no_delay")
                .valueRoute("max_seq_no_delay")
                .computeType(NONE).build());

        // global_checkpoint延迟量
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.global_checkpoint_delay")
                .valueRoute("global_checkpoint_delay")
                .computeType(NONE)
                .sendToOdin()
                .build());

        // shard中最少的可用的bulk队列
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.min_available_send_bulk_number")
                .valueRoute("min_available_send_bulk_number")
                .computeType(NONE)
                .sendToOdin()
                .build());

        // 总的发送时间
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.total_send_time_millis")
                .valueRoute("total_send_time_millis")
                .computeType(AVG).build());

        // 总的发送请求数
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.total_send_request")
                .valueRoute("total_send_request")
                .computeType(AVG).build());

        // 发送失败数
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.failed_send_requests")
                .valueRoute("failed_send_requests")
                .computeType(AVG)
                .sendToOdin().build());

        // 发送的bulk请求数
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.operations_send")
                .valueRoute("operations_send")
                .computeType(AVG)
                .sendToOdin().build());

        // 发送的总字节数
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.bytes_send")
                .valueRoute("bytes_send")
                .computeType(AVG).build());

        // 上次发送请求时间间隔最小的shard时间
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.min_time_since_last_send_millis")
                .valueRoute("min_time_since_last_send_millis")
                .computeType(NONE).build());

        // 上次发送请求时间间隔最大的shard时间
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.max_time_since_last_send_millis")
                .valueRoute("max_time_since_last_send_millis")
                .computeType(NONE)
                .sendToOdin().build());

        // 上次更新checkpoint时间间隔最大的shard时间
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.max_time_since_update_replica_checkpoint")
                .valueRoute("max_time_since_update_replica_checkpoint")
                .computeType(NONE)
                .sendToOdin().build());

        // 成功的恢复数量
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.success_recover_count")
                .valueRoute("success_recover_count")
                .computeType(AVG).build());

        // 失败的恢复数量
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.failed_recover_count")
                .valueRoute("failed_recover_count")
                .computeType(AVG).build());

        // 总的恢复时间
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.recover_total_time_millis")
                .valueRoute("recover_total_time_millis")
                .computeType(AVG).build());

        // 在同步的translog数量
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.in_sync_translog_offset_size")
                .valueRoute("in_sync_translog_offset_size")
                .computeType(NONE).build());

        // 请求平均耗时
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.send_request_avg_time")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.indices.dcdr.total_send_time_millis")
                .deriveParam( ESDataTempBean.DIVISOR, "es.indices.dcdr.total_send_request")
                .computeType(DERIVE_DIVISION).build());

        // 平均延迟时间
        dcdrWorkOrders.add(new CollectMetrics.Builder()
                .valueName("es.indices.dcdr.global_checkpoint_delay_seconds")
                .deriveParam( ESDataTempBean.DIVIDEND, "es.indices.dcdr.global_checkpoint_delay")
                .deriveParam( ESDataTempBean.DIVISOR, "es.indices.dcdr.operations_send")
                .computeType(DERIVE_DIVISION)
                .sendToOdin().build());

        return dcdrWorkOrders;
    }
}
