package com.didi.cloud.fastdump.common.bean.stats;

import java.util.List;

import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;

import lombok.Data;

/**
 * Created by linyunan on 2022/9/5
 */
@Data
public class IndexMoveTaskStats extends BaseESMoveTaskStats {
    /***************************基本信息****************************/
    private String                      sourceIndex;
    private String                      sourceCluster;

    private String                      targetIndex;
    private String                      targetCluster;

    /***************************索引在节点层面（Shard）统计信息****************************/
    private Long                        totalDocumentNum;
    private Long                        succDocumentNum;
    private Long                        failedDocumentNum;
    private Long                        costTime;

    private List<String>                allShardDataPath;
    private List<FailedShardInfoStatus> failedShardInfoStatus;

    /**
     * 索引总shard数
     */
    private Integer                     shardNum;
    /**
     * 成功的shard数量
     */
    private Integer                     succShardNum;

    /**
     * 成功、进行中、失败
     * @see TaskStatusEnum
     */
    private String                      status;

    /***************************各个节点层面信息****************************/
    /**
     * 读取文件限流值
     */
    private Long                        readFileRateLimit;

    /**
     * 内核计算出的读取文件限流值
     */
    private Long                         kernelEstimationReadFileRateLimit;

    @Override
    public String getKey() {
        return this.sourceIndex + "@" + this.sourceCluster + "@" + this.targetIndex + "@" + this.targetCluster;
    }
}
