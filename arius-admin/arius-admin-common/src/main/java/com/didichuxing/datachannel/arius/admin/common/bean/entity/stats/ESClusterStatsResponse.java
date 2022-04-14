package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.unit.ByteSizeValue;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESClusterStatsResponse {

    /**
     * 状态
     */
    private String        status;

    /**
     * 索引个数
     */
    private long          indexCount;
    /**
     * shard总个数
     */
    private long          totalShard;
    /**
     * 文档个数
     */
    private long          docsCount;
    /**
     * 总节点个数
     */
    private long          totalNodes;
    private long          numberDataNodes;
    private long          numberMasterNodes;
    private long          numberClientNodes;
    private long          numberCoordinatingOnly;
    private long          numberIngestNodes;

    /**********************内存********************/
    private ByteSizeValue memUsed;
    private ByteSizeValue memFree;
    private ByteSizeValue memTotal;
    private long          memUsedPercent;
    private long          memFreePercent;

    /**
     * 磁盘总大小
     */
    private ByteSizeValue totalFs;
    /**
     * 剩余磁盘大小
     */
    private ByteSizeValue freeFs;

    /**
     * 最大堆内存空间
     */
    private ByteSizeValue totalHeapMem;

    /**
     * 已使用的堆内存空间
     */
    private ByteSizeValue usedHeapMem;
}
