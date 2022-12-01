package com.didi.cloud.fastdump.common.bean.stats;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;
import com.google.common.util.concurrent.RateLimiter;

import lombok.Data;

/**
 * Created by linyunan on 2022/9/6
 */
@Data
public class IndexNodeMoveTaskStats extends BaseESMoveTaskStats {
    /***************************节点的索引信息****************************/
    private String                       sourceIndex;
    private String                       sourceCluster;

    private String                       targetIndex;
    private String                       targetCluster;
    /***************************节点的shard信息****************************/
    /**
     * 当前节点索引的shard数量
     */
    private Integer                      shardNum                    = 0;
    private Integer                      succShardNum                = 0;

    private List<String>                 allShardDataPath;
    private CopyOnWriteArrayList<String> failedShardDataPath;
    /***************************任务信息****************************/
    private Long                         totalDocumentNum            = 0L;
    private AtomicLong                   succDocumentNum;
    private AtomicLong                   failedDocumentNum;
    /**
     * 完成节点层面sinker任务耗时
     */
    private Long                         costTime                    = 0L;
    /**
     * @see TaskStatusEnum
     */
    private String                       status;
    private Integer                      statusCode;

    private String                       detail;

    /***************************各个节点层面信息****************************/
    /**
     * 自定义文件读速率标识
     */
    private AtomicBoolean                customReadFileRateLimitFlag = new AtomicBoolean(false);

    /**
     * 读取文件限流值
     */
    private AtomicLong                   readFileRateLimit;

    /**
     * 内核计算出的读取文件限流值
     */
    private Long                         kernelEstimationReadFileRateLimit;
    /**
     * lucene index(es shard) 级别写入引限流器
     */
    private RateLimiter                  readRateLimiter;

    @Override
    public String getKey() {
        return this.sourceIndex + "@" + this.sourceCluster + "@" + this.targetIndex + "@" + this.targetCluster;
    }
}
