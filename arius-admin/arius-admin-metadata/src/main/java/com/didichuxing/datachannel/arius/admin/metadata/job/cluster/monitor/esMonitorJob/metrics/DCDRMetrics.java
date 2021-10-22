package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.metrics;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author weizijun
 * date：2020-01-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DCDRMetrics {
    /**
     * max_seq_no延迟量
     */
    @JSONField(name = "max_seq_no_delay")
    private long maxSeqNoDelay;

    /**
     * global_checkpoint延迟量
     */
    @JSONField(name = "global_checkpoint_delay")
    private long globalCheckpointDelay;

    /**
     * shard中最少的可用的bulk队列
     */
    @JSONField(name = "min_available_send_bulk_number")
    private long minAvailableSendBulkNumber = Long.MAX_VALUE;

    /**
     * 总的发送时间
     */
    @JSONField(name = "total_send_time_millis")
    private long totalSendTimeMillis;

    /**
     * 总的发送请求数
     */
    @JSONField(name = "total_send_request")
    private long totalSendRequests;

    /**
     * 发送失败数
     */
    @JSONField(name = "failed_send_requests")
    private long failedSendRequests;

    /**
     * 发送的bulk请求数
     */
    @JSONField(name = "operations_send")
    private long operationsSend;

    /**
     * 发送的总字节数
     */
    @JSONField(name = "bytes_send")
    private long bytesSend;

    /**
     * 上次发送请求时间间隔最小的shard时间
     */
    @JSONField(name = "min_time_since_last_send_millis")
    private long minTimeSinceLastSendMillis = Long.MAX_VALUE;

    /**
     * 上次发送请求时间间隔最大的shard时间
     */
    @JSONField(name = "max_time_since_last_send_millis")
    private long maxTimeSinceLastSendMillis = -1;

    /**
     * 成功的恢复数量
     */
    @JSONField(name = "success_recover_count")
    private long successRecoverCount;

    /**
     * 失败的恢复数量
     */
    @JSONField(name = "failed_recover_count")
    private long failedRecoverCount;

    /**
     * 总的恢复时间
     */
    @JSONField(name = "recover_total_time_millis")
    private long recoverTotalTimeMillis;

    /**
     * 在同步的translog数量
     */
    @JSONField(name = "in_sync_translog_offset_size")
    private long inSyncTranslogOffsetSize;

    /**
     * 上次更新checkpoint时间间隔最大的shard时间
     */
    @JSONField(name = "max_time_since_update_replica_checkpoint")
    private long maxTimeSinceUpdateReplicaCheckPoint;
}
